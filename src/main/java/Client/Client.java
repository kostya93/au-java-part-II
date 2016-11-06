package Client;

import Common.PartOfFile;
import Common.SharedFile;
import Common.Source;

import java.io.File;
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
 *   - Get: receives a list part of file
 *
 *  The client has methods start() and stop() because it acts
 *  as a server for other clients
 */
public interface Client {
    void start(int port);
    void stop();

    List<SharedFile> executeList();
    int executeUpload(File file);
    List<Source> executeSources(int fileId);
    boolean executeUpdate();

    List<Integer> executeStat(Source source, int fileId);
    PartOfFile executeGet(Source source, int fileId, int numberOfPart);
}
