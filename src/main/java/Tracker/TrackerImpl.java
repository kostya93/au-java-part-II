package Tracker;

import Common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import static java.lang.Thread.sleep;

public class TrackerImpl implements Tracker {
    private final int UPDATE_INTERVAL = 1000 * 60 * 5;

    private File rootDir;

    private ServerSocket serverSocket;
    private final List<Thread> clientThreads = new LinkedList<>();
    private Thread serverThread;

    private Thread sourceCleaner;

    private final String STATE_FILE_FILENAME = "state";
    final private List<SharedFile> sharedFiles = new LinkedList<>();
    final private Map<Integer, Set<Source>> sources = new HashMap<>();

    public TrackerImpl() {}


    @Override
    public void start(int port, File rootDir) throws SocketIOException, SerializationException {
        this.rootDir = rootDir;
        if (!this.rootDir.exists() || !this.rootDir.isDirectory()) {
            throw new RootDirectoryNotFound(rootDir.toString());
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new SocketIOException("Cant create socket on port  = \"" + port + "\";");
        }
        restoreState();

        sourceCleaner = new Thread(this::cleanSources);
        sourceCleaner.start();
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
        sourceCleaner.interrupt();

        storeState();
    }

    private void restoreState() throws SerializationException {
        try {
            File stateFile = new File(rootDir, STATE_FILE_FILENAME);
            if (!stateFile.exists()) {
                return;
            }
            FileInputStream fileInputStream = new FileInputStream(stateFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            sharedFiles.addAll((List<SharedFile>) objectInputStream.readObject());
            sources.putAll((Map<Integer, Set<Source>>) objectInputStream.readObject());
        } catch (Exception e) {
            throw new SerializationException("cant restore state", e);
        }
    }

    private void storeState() throws SerializationException {
        try {
            File stateFile = new File(rootDir, STATE_FILE_FILENAME);
            FileOutputStream fileOutputStream = new FileOutputStream(stateFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(sharedFiles);
            objectOutputStream.writeObject(sources);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new SerializationException("cant store state", e);
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

    private void cleanSources() {
        while (true) {
            try {
                sleep(UPDATE_INTERVAL);
                long curTime = System.currentTimeMillis();
                synchronized (sources) {
                    for (Map.Entry<Integer, Set<Source>> entry: sources.entrySet()){
                        entry.getValue().removeIf(source -> curTime - source.getLastUpdate() > UPDATE_INTERVAL * 1000);
                    }
                }
            } catch (InterruptedException ignored) {
                return;
            }
        }
    }

    private void processClient(Socket clientSocket) {
        try {
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            byte type = dataInputStream.readByte();
            switch (type) {
                case TypeOfRequestToTracker.LIST:
                    processList(dataOutputStream);
                    break;
                case TypeOfRequestToTracker.SOURCES:
                    processSources(dataInputStream, dataOutputStream);
                    break;
                case TypeOfRequestToTracker.UPDATE:
                    processUpdate(dataInputStream, dataOutputStream, clientSocket.getInetAddress().getAddress());
                    break;
                case TypeOfRequestToTracker.UPLOAD:
                    processUpload(dataInputStream, dataOutputStream);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processList(DataOutputStream dataOutputStream) throws IOException {
        synchronized (sharedFiles) {
            dataOutputStream.writeInt(sharedFiles.size());
            for (SharedFile sharedFile: sharedFiles) {
                dataOutputStream.writeInt(sharedFile.getId());
                dataOutputStream.writeUTF(sharedFile.getName());
                dataOutputStream.writeLong(sharedFile.getSize());
            }
            dataOutputStream.flush();
        }
    }
    private void processUpload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        String name = dataInputStream.readUTF();
        long size = dataInputStream.readLong();
        int id;
        synchronized (sharedFiles) {
            id = sharedFiles.size();
            sharedFiles.add(new SharedFile(name, id, size));
        }
        synchronized (sources) {
            sources.put(id, new LinkedHashSet<>());
        }
        dataOutputStream.writeInt(id);
        dataOutputStream.flush();
    }
    private void processSources(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int fileId = dataInputStream.readInt();
        synchronized (sources) {
            Set<Source> sources = this.sources.get(fileId);
            if (sources == null) {
                dataOutputStream.writeInt(0);
            } else {
                dataOutputStream.writeInt(sources.size());
                for (Source source : sources) {
                    byte[] ip = source.getIp();
                    for (int i = 0; i < Source.IP_LENGTH; i++) {
                        dataOutputStream.writeByte(ip[i]);
                    }
                    dataOutputStream.writeInt(source.getPort());
                }
            }
            dataOutputStream.flush();
        }
    }
    private void processUpdate(DataInputStream dataInputStream, DataOutputStream dataOutputStream, byte[] ip) throws IOException {
        int  port = dataInputStream.readInt();
        int numOfFiles = dataInputStream.readInt();
        List<Integer> fileIds = new LinkedList<>();
        for (int i = 0; i < numOfFiles; i++) {
            fileIds.add(dataInputStream.readInt());
        }
        Source curSource = new Source(ip, port);
        synchronized (sources) {
            for (Integer id: fileIds) {
                Set<Source> sources = this.sources.get(id);
                sources.remove(curSource);
                sources.add(curSource);
            }
        }
        dataOutputStream.writeBoolean(true);
        dataOutputStream.flush();
    }
}
