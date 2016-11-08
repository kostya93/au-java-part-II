package Tracker;

import Common.SharedFile;
import Common.SocketIOException;
import Common.Source;
import Common.TypeOfRequestToTracker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import static java.lang.Thread.sleep;

public class TrackerImpl implements Tracker {
    private final int UPDATE_INTERVAL = 1000 * 60 * 5;

    private ServerSocket serverSocket;
    private final List<Thread> clientThreads = new LinkedList<>();
    private Thread serverThread;
    private Thread sourceCleaner;

    private final String STATE_FILE_FILENAME = "state";

    private List<SharedFile> sharedFiles = new LinkedList<>();
    private Map<Integer, Set<Source>> fileIdToSources = new HashMap<>();

    private final File rootDir;

    public TrackerImpl(File rootDir) {
        this.rootDir = rootDir;
    }


    @Override
    public void start() throws SocketIOException, SerializationException {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            throw new SocketIOException("Cant create socket on port  = \"" + PORT + "\";");
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
            sharedFiles = (List<SharedFile>) objectInputStream.readObject();
            fileIdToSources = (Map<Integer, Set<Source>>) objectInputStream.readObject();
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
            objectOutputStream.writeObject(fileIdToSources);
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
                synchronized (fileIdToSources) {
                    for (Map.Entry<Integer, Set<Source>> entry: fileIdToSources.entrySet()){
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
        } catch (IOException ignored) {
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
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
        synchronized (fileIdToSources) {
            fileIdToSources.put(id, new LinkedHashSet<>());
        }
        dataOutputStream.writeInt(id);
        dataOutputStream.flush();
    }
    private void processSources(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int fileId = dataInputStream.readInt();
        synchronized (fileIdToSources) {
            Set<Source> sources = fileIdToSources.get(fileId);
            dataOutputStream.writeInt(sources.size());
            for (Source source : sources) {
                dataOutputStream.write(source.getIp());
                dataOutputStream.writeShort(source.getPort());
            }
        }
    }
    private void processUpdate(DataInputStream dataInputStream, DataOutputStream dataOutputStream, byte[] ip) throws IOException {
        short port = dataInputStream.readShort();
        int numOfFiles = dataInputStream.readInt();
        List<Integer> fileIds = new LinkedList<>();
        for (int i = 0; i < numOfFiles; i++) {
            fileIds.add(dataInputStream.readInt());
        }
        Source curSource = new Source(ip, port);
        synchronized (fileIdToSources) {
            for (Integer id: fileIds) {
                Set<Source> sources = fileIdToSources.get(id);
                sources.remove(curSource);
                sources.add(curSource);
            }
        }
        dataOutputStream.writeBoolean(true);
        dataOutputStream.flush();
    }
}
