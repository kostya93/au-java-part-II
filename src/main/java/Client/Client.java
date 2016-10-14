package Client;

import java.util.List;

/**
 * Created by kostya on 14.10.2016.
 */
public interface Client {
    int connect(String host, int port);
    int disconnect();
    List<MyFile> executeList(String dirPath);
    byte[] executeGet(String filePath);
}
