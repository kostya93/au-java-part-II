package Client;

import Common.SocketIOException;
import Common.TypeOfRequest;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kostya on 14.10.2016.
 */
public class ClientImpl implements Client {
    private SocketAddress socketAddress;
    @Override
    public void connect(String host, int port) {
        socketAddress = new InetSocketAddress(host, port);
    }

    @Override
    public void disconnect() {
        socketAddress = null;
    }

    @Override
    public List<MyFile> executeList(String dirPath) {
        if (socketAddress == null) {
            return null;
        }

        try {
            Socket clientSocket = new Socket();
            clientSocket.connect(socketAddress);
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            dataOutputStream.writeInt(TypeOfRequest.LIST);
            dataOutputStream.writeUTF(dirPath);
            dataOutputStream.flush();
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            int size = dataInputStream.readInt();
            List<MyFile> result = new LinkedList<>();
            for (int i = 0; i < size; i++) {
                String filename =  dataInputStream.readUTF();
                boolean isDir = dataInputStream.readBoolean();
                result.add(new MyFile(filename, isDir));
            }
            clientSocket.close();
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public byte[] executeGet(String filePath) {
        if (socketAddress == null) {
            return null;
        }

        try {
            Socket clientSocket = new Socket();
            clientSocket.connect(socketAddress);
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            dataOutputStream.writeInt(TypeOfRequest.GET);
            dataOutputStream.writeUTF(filePath);
            dataOutputStream.flush();
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            long size = dataInputStream.readLong();
            if (size == 0) {
                return null;
            }
            long reads = 0;
            byte buffer[] = new byte[1024];
            int read;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while(reads < size){
                read = dataInputStream.read(buffer);
                if (read > 0) {
                    byteArrayOutputStream.write(buffer, 0, read);
                    reads += read;
                }
            }
            clientSocket.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
