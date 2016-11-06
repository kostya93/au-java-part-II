package Common;

/**
 * Class PartOfFile represents a part of file.
 * - SIZE: size in bytes.
 * - fileId: id of file that includes this part
 * - positionInFile: positions this part in file
 * - content: content of this part (array of bytes)
 */
public class PartOfFile {
    public static final int SIZE = 10_000_000;
    private final int fileId;
    private final int positionInFile;
    private final byte[] content;

    public PartOfFile(int fileId, int positionInFile, byte[] content) {
        this.fileId = fileId;
        this.positionInFile = positionInFile;
        this.content = content;
    }

    public int getFileId() {
        return fileId;
    }

    public int getPositionInFile() {
        return positionInFile;
    }

    public byte[] getContent() {
        return content;
    }
}
