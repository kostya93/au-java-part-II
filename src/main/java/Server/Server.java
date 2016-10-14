package Server;

import java.io.File;

/**
 * Created by kostya on 14.10.2016.
 */
public interface Server {
    int start(int port, File rootDir);
    int stop();
}
