package Client;

import Common.PartOfFile;
import Common.SharedFile;
import Common.Source;

import java.io.File;
import java.util.List;

/**
 * Created by kostya on 06.11.2016.
 */
public class ClientImpl implements Client {
    @Override
    public void start(int port) {

    }

    @Override
    public void stop() {

    }

    @Override
    public List<SharedFile> executeList() {
        return null;
    }

    @Override
    public int executeUpload(File file) {
        return 0;
    }

    @Override
    public List<Source> executeSources(int fileId) {
        return null;
    }

    @Override
    public boolean executeUpdate() {
        return false;
    }

    @Override
    public List<Integer> executeStat(Source source, int fileId) {
        return null;
    }

    @Override
    public PartOfFile executeGet(Source source, int fileId, int numberOfPart) {
        return null;
    }
}
