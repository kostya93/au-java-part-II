package Common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class PartOfFile represents a part of file.
 * - MAX_SIZE: max size of patr in bytes.
 * - size: real size.
 * - fileId: id of file that includes this part
 * - positionInFile: positions this part in file
 * - content: content of this part (array of bytes)
 */
public class PartOfFile {
    public static final int MAX_SIZE = 10_000_000;
    private final int size;
    private final int fileId;
    private final int positionInFile;

    public PartOfFile(int size, int fileId, int positionInFile) {
        this.size = size;
        this.fileId = fileId;
        this.positionInFile = positionInFile;
    }

    public int getFileId() {
        return fileId;
    }

    public int getPositionInFile() {
        return positionInFile;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(fileId)
                .append(positionInFile)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PartOfFile))
            return false;
        if (obj == this)
            return true;

        PartOfFile rhs = (PartOfFile) obj;
        return new EqualsBuilder()
                .append(fileId, rhs.getFileId())
                .append(positionInFile, rhs.getPositionInFile())
                .isEquals();
    }
}
