package Client;

import Common.*;
import Tracker.SerializationException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by kostya on 06.11.2016.
 */
public class ClientImpl implements Client {
    private final String STATE_FILE_FILENAME = "state";
    private final String FILES_DIR_NAME = "files";

    private ServerSocket serverSocket;
    private final int port;

    private final File rootDir;
    private final List<Thread> clientThreads = new LinkedList<>();
    private Thread serverThread;
    private FileSaver fileSaver;
    private Map<Integer, Set<PartOfFile>> fileIdToPartsOfFile = new HashMap<>();
    private Map<Integer, String> fileIdToLocalFiles = new HashMap<>();

    public ClientImpl(int port, File rootDir) {
        this.port = port;
        this.rootDir = rootDir;
        File dirForFiles = new File(this.rootDir, FILES_DIR_NAME);
        dirForFiles.mkdirs();
        fileSaver = new FileSaver(dirForFiles);
    }

    @Override
    public void start() throws SocketIOException, SerializationException {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new SocketIOException("Cant create socket on port  = \"" + port + "\";");
        }
        restoreState();

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

        clientThreads.forEach(Thread::interrupt);
        clientThreads.clear();
        serverThread.interrupt();

        storeState();
    }

    private void runServer() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> processClient(clientSocket));
                clientThreads.add(clientThread);
                clientThread.start();
            }
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private void processStat(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int fileId = dataInputStream.readInt();
        int count;
        List<Integer> parts = new ArrayList<>();
        synchronized (fileIdToPartsOfFile) {
            if (fileIdToPartsOfFile.containsKey(fileId)) {
                count = fileIdToPartsOfFile.get(fileId).size();
                parts = fileIdToPartsOfFile.get(fileId)
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
        synchronized (fileIdToLocalFiles) {
            if (fileIdToLocalFiles.containsKey(fileId)) {
                file = new File(fileIdToLocalFiles.get(fileId));
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
            res.add(new SharedFile(name, id, size));
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
        synchronized (fileIdToLocalFiles) {
            fileIdToLocalFiles.put(id, file.getAbsolutePath());
        }
        synchronized (fileIdToPartsOfFile) {
            fileIdToPartsOfFile.put(id, new HashSet<>());
            long curSize = 0;
            int i = 0;
            while (curSize < file.length()) {
                PartOfFile part = new PartOfFile((int) Math.min(file.length() - curSize, PartOfFile.MAX_SIZE), id, i);
                fileIdToPartsOfFile.get(id).add(part);
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
            short port = dataInputStream.readShort();
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
        dataOutputStream.writeShort(port);
        dataOutputStream.writeInt(fileIdToPartsOfFile.size());
        for (Integer id: fileIdToPartsOfFile.keySet()) {
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
        synchronized (fileIdToPartsOfFile) {
            if (!fileIdToPartsOfFile.containsKey(sharedFile.getId())) {
                fileIdToPartsOfFile.put(sharedFile.getId(), new HashSet<>());
            }
            fileIdToPartsOfFile.get(sharedFile.getId()).add(partOfFile);
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
            fileIdToLocalFiles = (Map<Integer, String>) objectInputStream.readObject();
            fileIdToPartsOfFile = (Map<Integer, Set<PartOfFile>>) objectInputStream.readObject();
        } catch (Exception e) {
            throw new SerializationException("cant restore state", e);
        }
    }

    private void storeState() throws SerializationException {
        try {
            File stateFile = new File(rootDir, STATE_FILE_FILENAME);
            FileOutputStream fileOutputStream = new FileOutputStream(stateFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(fileIdToLocalFiles);
            objectOutputStream.writeObject(fileIdToPartsOfFile);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new SerializationException("cant store state", e);
        }
    }
}
