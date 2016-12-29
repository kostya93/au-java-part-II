package Server;

import Common.SocketIOException;

import java.io.File;

/**
 * Created by kostya on 14.10.2016.
 */
public interface Server {
    void start(int port, File rootDir) throws RootDirectoryNotFound, SocketIOException;
    void stop() throws SocketIOException;
}
