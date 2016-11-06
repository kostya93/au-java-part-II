package Client;

import Common.SocketIOException;

import java.util.List;

/**
 * Created by kostya on 14.10.2016.
 */
public interface Client {
    void connect(String host, int port) throws SocketIOException;
    void disconnect() throws SocketIOException;
    List<MyFile> executeList(String dirPath);
    byte[] executeGet(String filePath);
}
