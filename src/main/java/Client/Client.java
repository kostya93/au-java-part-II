package Client;

import Common.SerializationException;
import Common.SharedFile;
import Common.SocketIOException;
import Common.Source;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class Client represents a torrent client;
 *
 * Client can make  requests to the server:
 *  - List: receives a list of shared files
 *  - Upload: upload info about new shared file
 *  - Sources: receives a list of sources that own file
 *  - Update: send info about all shared files to the server
 *
 *  Client can make  requests to the other client:
 *   - Stat: receives a list of available file parts
 *   - Get: receives a part of file
 *
 *  The client has methods start() and stop() because it acts
 *  as a server for other clients
 */
public interface Client {
    void start(int port, File rootDir) throws SocketIOException, SerializationException;
    void stop() throws SocketIOException, SerializationException;

    List<SharedFile> executeList(String serverHost, int serverPort) throws IOException;
    int executeUpload(String serverHost, int serverPort, File file) throws IOException;
    List<Source> executeSources(String serverHost, int serverPort, int fileId) throws IOException;
    boolean executeUpdate(String serverHost, int serverPort) throws IOException;

    List<Integer> executeStat(Source source, int fileId) throws IOException;
    void executeGet(Source source, SharedFile sharedFile, int numberOfPart) throws IOException;

    void addFileToDownloading(String serverHost, int serverPort, SharedFile sharedFile);
    List<DownloadingFileState> downloadingState();

    boolean isStarted();
}
