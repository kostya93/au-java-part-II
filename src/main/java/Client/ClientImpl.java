package Client;

import Common.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Created by kostya on 06.11.2016.
 */
public class ClientImpl implements Client {
    private final String STATE_FILE_FILENAME = "state";
    private final String FILES_DIR_NAME = "files";

    private ServerSocket serverSocket;
    private int port;
    private File rootDir;

    private final List<Thread> clientThreads = new LinkedList<>();
    private Thread serverThread;
    private Thread downloader;
    private FileSaver fileSaver;
    final private Map<Integer, Set<PartOfFile>> fileParts = new HashMap<>();
    final private Map<Integer, String> localFiles = new HashMap<>();
    final private Map<Integer, SharedFile> sharedFiles = new HashMap<>();
    final private LinkedList<DownloadTask> downloadingQueue = new LinkedList<>();

    public ClientImpl() {}

    @Override
    public void start(int port, File rootDir) throws SocketIOException, SerializationException {
        this.rootDir = rootDir;
        this.port = port;
        if (!this.rootDir.exists() || !this.rootDir.isDirectory()) {
            throw new RootDirectoryNotFound(rootDir.toString());
        }

        File dirForFiles = new File(this.rootDir, FILES_DIR_NAME);
        dirForFiles.mkdirs();
        fileSaver = new FileSaver(dirForFiles);

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new SocketIOException("Cant create socket on port  = \"" + port + "\";");
        }
        restoreState();

        downloader = new Thread(this::downloading);
        downloader.start();
        serverThread = new Thread(this::runServer);
        serverThread.start();
    }

    @Override
    public void stop() throws SocketIOException, SerializationException {
        if (serverSocket == null) {
            return;
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new SocketIOException("Cant close socket \"" + serverSocket + "\"");
        }

        downloader.interrupt();
        clientThreads.forEach(Thread::interrupt);
        clientThreads.clear();
        serverThread.interrupt();

        storeState();
    }

    @Override
    public void addFileToDownloading(String serverHost, int serverPort, SharedFile sharedFile) {
        if (isFileDownloaded(sharedFile)) {
            return;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
        List<PartOfFile> parts = getMissingParts(sharedFile);
        synchronized (downloadingQueue) {
            for (PartOfFile part: parts) {
                downloadingQueue.add(new DownloadTask(inetSocketAddress, part, sharedFile));
            }
        }
    }

    @Override
    public List<DownloadingFileState> downloadingState() {
        List<DownloadingFileState> res = new LinkedList<>();
        synchronized (fileParts) {
            for (Map.Entry<Integer, Set<PartOfFile>> file : fileParts.entrySet()) {
                SharedFile sharedFile;
                synchronized (sharedFiles) {
                    sharedFile = sharedFiles.get(file.getKey());
                }
                double progress = ((double) file.getValue().size()) / sharedFile.numOfParts();
                String path;
                synchronized (localFiles) {
                    path = localFiles.get(file.getKey());
                }
                if (path == null) {
                    try {
                        path = fileSaver.getFileByFileId(file.getKey()).getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        path = "unknown";
                    }
                }
                res.add(new DownloadingFileState(sharedFile, progress, path));
            }
        }
        return res;
    }

    private void downloading() {
        while (!Thread.currentThread().isInterrupted()) {
            boolean isEmpty;
            synchronized (downloadingQueue){
                isEmpty = downloadingQueue.isEmpty();
            }
            if (isEmpty) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }
            DownloadTask downloadTask;
            synchronized (downloadingQueue) {
                downloadTask = downloadingQueue.pop();
            }
            if (isPartDownloaded(downloadTask.getPart())) {
                continue;
            }
            if (!getPart(downloadTask.getSharedFile(), downloadTask.getPart(), downloadTask.getInetSocketAddress())) {
                synchronized (downloadingQueue) {
                    downloadingQueue.add(downloadTask);
                }
            }
        }
    }

    private boolean getPart(SharedFile sharedFile, PartOfFile partOfFile, InetSocketAddress server) {
        try {
            List<Source> sources = executeSources(server.getHostString(), server.getPort(), partOfFile.getFileId());
            for (Source source: sources) {
                List<Integer> parts = executeStat(source, partOfFile.getFileId());
                if (parts.contains(partOfFile.getPositionInFile())) {
                    executeGet(source, sharedFile, partOfFile.getPositionInFile());
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private boolean isPartDownloaded(PartOfFile partOfFile) {
        synchronized (fileParts) {
            return fileParts.get(partOfFile.getFileId()) != null && fileParts.get(partOfFile.getFileId()).contains(partOfFile);
        }
    }

    private boolean isFileDownloaded(SharedFile sharedFile) {
        synchronized (fileParts) {
            return fileParts.get(sharedFile.getId()) != null && sharedFile.numOfParts() == fileParts.get(sharedFile.getId()).size();
        }
    }

    private List<PartOfFile> getMissingParts(SharedFile sharedFile) {
        List<PartOfFile> res = new LinkedList<>();
        synchronized (fileParts) {
            Set<PartOfFile> partOfFiles = fileParts.get(sharedFile.getId());
            
            if (partOfFiles == null) {
                for (int i = 0; i < sharedFile.numOfParts(); i++) {
                    res.add(new PartOfFile(sharedFile.getSizeOfPart(i), sharedFile.getId(), i));
                }
                return res;
            }
            
            for (int i = 0; i < sharedFile.numOfParts(); i++) {
                PartOfFile partOfFile = new PartOfFile(sharedFile.getSizeOfPart(i), sharedFile.getId(), i);
                if (!partOfFiles.contains(partOfFile)) {
                    res.add(new PartOfFile(sharedFile.getSizeOfPart(i), sharedFile.getId(), i));
                }
            }
            return res;
        }
    }

    private void runServer() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> processClient(clientSocket));
                clientThreads.add(clientThread);
                clientThread.start();
            }
        } catch (SocketException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClient(Socket clientSocket) {
        try {
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            byte type = dataInputStream.readByte();
            switch (type) {
                case TypeOfRequestToClient.STAT:
                    processStat(dataInputStream, dataOutputStream);
                    break;
                case TypeOfRequestToClient.GET:
                    processGet(dataInputStream, dataOutputStream);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processStat(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int fileId = dataInputStream.readInt();
        int count;
        List<Integer> parts = new ArrayList<>();
        synchronized (fileParts) {
            if (fileParts.containsKey(fileId)) {
                count = fileParts.get(fileId).size();
                parts = fileParts.get(fileId)
                        .stream()
                        .map(PartOfFile::getPositionInFile)
                        .collect(Collectors.toList());
            } else {
                count = 0;
            }
        }
        dataOutputStream.writeInt(count);
        for (int i = 0; i < count; i++) {
            dataOutputStream.writeInt(parts.get(i));
        }
        dataOutputStream.flush();
    }

    private void processGet(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int fileId = dataInputStream.readInt();
        int position = dataInputStream.readInt();
        File file;
        synchronized (localFiles) {
            if (localFiles.containsKey(fileId)) {
                file = new File(localFiles.get(fileId));
            } else {
                file = fileSaver.getFileByFileId(fileId);
            }
        }
        fileSaver.copyFilePartToStream(file, position, PartOfFile.MAX_SIZE, dataOutputStream);
        dataOutputStream.flush();
    }

    @Override
    public List<SharedFile> executeList(String serverHost, int serverPort) throws IOException {
        Socket socket = new Socket(serverHost, serverPort);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeByte(TypeOfRequestToTracker.LIST);
        dataOutputStream.flush();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        List<SharedFile> res = new ArrayList<>();
        int count = dataInputStream.readInt();
        for (int i = 0; i < count; i++) {
            int id = dataInputStream.readInt();
            String name = dataInputStream.readUTF();
            long size = dataInputStream.readLong();
            SharedFile sharedFile = new SharedFile(name, id, size);
            res.add(sharedFile);
        }
        socket.close();
        return res;
    }

    @Override
    public int executeUpload(String serverHost, int serverPort, File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException();
        }

        Socket socket = new Socket(serverHost, serverPort);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeByte(TypeOfRequestToTracker.UPLOAD);
        dataOutputStream.writeUTF(file.getName());
        dataOutputStream.writeLong(file.length());
        dataOutputStream.flush();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        int id = dataInputStream.readInt();
        synchronized (localFiles) {
            localFiles.put(id, file.getAbsolutePath());
        }
        SharedFile sharedFile = new SharedFile(file.getName(), id, file.length());
        synchronized (sharedFiles) {
            sharedFiles.put(id, sharedFile);
        }
        synchronized (fileParts) {
            fileParts.put(id, new HashSet<>());
            long curSize = 0;
            int i = 0;
            while (curSize < file.length()) {
                PartOfFile part = new PartOfFile((int) Math.min(file.length() - curSize, PartOfFile.MAX_SIZE), id, i);
                fileParts.get(id).add(part);
                curSize += part.getSize();
                i++;
            }
        }
        socket.close();
        return id;
    }

    @Override
    public List<Source> executeSources(String serverHost, int serverPort, int fileId) throws IOException {
        Socket socket = new Socket(serverHost, serverPort);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeByte(TypeOfRequestToTracker.SOURCES);
        dataOutputStream.writeInt(fileId);
        dataOutputStream.flush();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        List<Source> res = new ArrayList<>();
        int count = dataInputStream.readInt();
        for (int i = 0; i < count; i++) {
            byte[] ip = new byte[Source.IP_LENGTH];
            for (int j = 0; j < Source.IP_LENGTH; j++) {
                ip[j] = dataInputStream.readByte();
            }
            int port = dataInputStream.readInt();
            res.add(new Source(ip, port));
        }
        socket.close();
        return res;
    }

    @Override
    public boolean executeUpdate(String serverHost, int serverPort) throws IOException {
        Socket socket = new Socket(serverHost, serverPort);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeByte(TypeOfRequestToTracker.UPDATE);
        dataOutputStream.writeInt(port);
        dataOutputStream.writeInt(fileParts.size());
        for (Integer id: fileParts.keySet()) {
            dataOutputStream.writeInt(id);
        }
        dataOutputStream.flush();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        boolean res = dataInputStream.readBoolean();
        socket.close();
        return res;
    }

    @Override
    public List<Integer> executeStat(Source source, int fileId) throws IOException {
        Socket socket = new Socket(source.getHost(), source.getPort());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeByte(TypeOfRequestToClient.STAT);
        dataOutputStream.writeInt(fileId);
        dataOutputStream.flush();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        int count = dataInputStream.readInt();
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            res.add(dataInputStream.readInt());
        }
        socket.close();
        return res;
    }

    @Override
    public void executeGet(Source source, SharedFile sharedFile, int position) throws IOException {
        Socket socket = new Socket(source.getHost(), source.getPort());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeByte(TypeOfRequestToClient.GET);
        dataOutputStream.writeInt(sharedFile.getId());
        dataOutputStream.writeInt(position);
        dataOutputStream.flush();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        PartOfFile partOfFile = new PartOfFile(sharedFile.getSizeOfPart(position), sharedFile.getId(), position);
        fileSaver.copyFilePartFromStream(dataInputStream, sharedFile.getId(), partOfFile.getPositionInFile(),
                    partOfFile.getSize());
        synchronized (sharedFiles) {
            sharedFiles.putIfAbsent(sharedFile.getId(), sharedFile);
        }
        synchronized (fileParts) {
            if (!fileParts.containsKey(sharedFile.getId())) {
                fileParts.put(sharedFile.getId(), new HashSet<>());
            }
            fileParts.get(sharedFile.getId()).add(partOfFile);
        }
        socket.close();
    }

    private void restoreState() throws SerializationException {
        try {
            File stateFile = new File(rootDir, STATE_FILE_FILENAME);
            if (!stateFile.exists()) {
                return;
            }
            FileInputStream fileInputStream = new FileInputStream(stateFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            localFiles.putAll((Map<Integer, String>) objectInputStream.readObject());
            fileParts.putAll((Map<Integer, Set<PartOfFile>>) objectInputStream.readObject());
            downloadingQueue.addAll((LinkedList<DownloadTask>)objectInputStream.readObject());
            sharedFiles.putAll((Map<Integer, SharedFile>)objectInputStream.readObject());
        } catch (Exception e) {
            throw new SerializationException("cant restore state", e);
        }
    }

    private void storeState() throws SerializationException {
        try {
            File stateFile = new File(rootDir, STATE_FILE_FILENAME);
            FileOutputStream fileOutputStream = new FileOutputStream(stateFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(localFiles);
            objectOutputStream.writeObject(fileParts);
            objectOutputStream.writeObject(downloadingQueue);
            objectOutputStream.writeObject(sharedFiles);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new SerializationException("cant store state", e);
        }
    }
}
