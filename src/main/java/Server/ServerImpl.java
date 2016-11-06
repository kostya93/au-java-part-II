package Server;

import Common.SocketIOException;
import Common.TypeOfRequest;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kostya on 14.10.2016.
 */
public class ServerImpl implements Server {
    private ServerSocket serverSocket;
    private List<Thread> clientThreads = new LinkedList<>();
    private File rootDir;
    private Thread serverThread;

    @Override
    public void start(int port, File rootDir) throws RootDirectoryNotFound, SocketIOException {
        this.rootDir = rootDir;
        if (!this.rootDir.exists() || !this.rootDir.isDirectory()) {
            throw new RootDirectoryNotFound(rootDir.toString());
        }
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            this.rootDir = null;
            throw new SocketIOException("Cant create socket on port  = \"" + port + "\";");
        }

        serverThread = new Thread(this::runServer);
        serverThread.start();
    }

    @Override
    public void stop() throws SocketIOException {
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
        serverThread = null;
        serverSocket = null;
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
            int type = dataInputStream.readInt();
            if (type == TypeOfRequest.GET) {
                processGet(dataInputStream, dataOutputStream);
            }
            else if (type == TypeOfRequest.LIST) {
                processList(dataInputStream, dataOutputStream);
            }
        } catch (IOException ignored) {
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private void processList(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        String path = dataInputStream.readUTF();
        File dir = new File(rootDir, path);
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            dataOutputStream.write(0);
            dataOutputStream.flush();
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            dataOutputStream.write(0);
            dataOutputStream.flush();
            return;
        }
        dataOutputStream.writeInt(files.length);
        for (File file: files) {
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.writeBoolean(file.isDirectory());
        }
        dataOutputStream.flush();
    }

    private void processGet(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        String path = dataInputStream.readUTF();
        File file = new File(rootDir, path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            dataOutputStream.write(0);
            dataOutputStream.flush();
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        dataOutputStream.writeLong(fileInputStream.available());
        int read;
        byte buffer[] = new byte[1024];
        while((read = fileInputStream.read(buffer)) != -1){
            dataOutputStream.write(buffer, 0, read);
        }
        dataOutputStream.flush();
    }

}
