package Client;

import Common.PartOfFile;
import Common.SharedFile;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by kostya on 02.12.2016.
 */
public class DownloadTask implements Serializable {
    private InetSocketAddress inetSocketAddress;
    private PartOfFile part;
    private SharedFile sharedFile;

    public DownloadTask(InetSocketAddress inetSocketAddress, PartOfFile part, SharedFile sharedFile) {
        this.inetSocketAddress = inetSocketAddress;
        this.part = part;
        this.sharedFile = sharedFile;
    }

    public PartOfFile getPart() {
        return part;
    }

    public SharedFile getSharedFile() {
        return sharedFile;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }
}
