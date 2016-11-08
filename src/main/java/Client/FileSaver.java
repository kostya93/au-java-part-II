package Client;

import Common.PartOfFile;

import java.io.*;
import java.nio.channels.Channels;

/**
 * class FileSaver manages the downloaded files on disk
 */
public class FileSaver {
    private final File rootDir;

    public FileSaver(File rootDir) {
        this.rootDir = rootDir;
    }

    public void copyFilePartToStream(File file, int position, int len, OutputStream out) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
            randomAccessFile.seek(position * PartOfFile.MAX_SIZE);
            copy(Channels.newInputStream(randomAccessFile.getChannel()), out, len);
        }
    }

    public void copyFilePartFromStream(InputStream in, int fileId, int position, int len) throws IOException {
        File file = new File(rootDir, Integer.toString(fileId));
        System.out.println(file.getAbsolutePath());
        if (!file.exists()) {
            file.createNewFile();
        }

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")){
            randomAccessFile.seek(position * PartOfFile.MAX_SIZE);
            copy(in, Channels.newOutputStream(randomAccessFile.getChannel()), len);
        }
    }

    public File getFileByFileId(int fileId) throws FileNotFoundException {
        File file = new File(rootDir, Integer.toString(fileId));
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return file;
    }


    private void copy(InputStream inputStream, OutputStream outputStream, int len) throws IOException {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;
        while (len > 0) {
            read = inputStream.read(buffer, 0, Math.min(BUFFER_SIZE, len));
            if (read == -1) {
                break;
            }
            outputStream.write(buffer, 0, read);
            len -= read;
        }
    }
}
