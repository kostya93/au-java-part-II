package Client;

import Common.SharedFile;

/**
 * Created by kostya on 02.12.2016.
 */

public class DownloadingFileState {
    private SharedFile sharedFile;
    private double progress;
    private String path;

    public DownloadingFileState(SharedFile sharedFile, double progress, String path) {
        this.sharedFile = sharedFile;
        this.progress = progress;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public double getProgress() {
        return progress;
    }

    public SharedFile getSharedFile() {
        return sharedFile;
    }

    @Override
    public String toString() {
        return String.format("%s; progress = %.1f%%; path = %s", sharedFile, progress * 100, path);
    }
}
