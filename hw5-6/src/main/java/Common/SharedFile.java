package Common;

import java.io.Serializable;

/**
 * Class SharedFile represents a shared file;
 *  - name: name of this file
 *  - id: id of this file
 *  - size: size of this file (in bytes)
 *  - sources: a set of sources that own file
 */
public class SharedFile  implements Serializable {
    private final String name;
    private final int id;
    private final long size;
    private final int numOfParts;

    public SharedFile(String name, int id, long size) {
        this.name = name;
        this.id = id;
        this.size = size;
        this.numOfParts = (int) (size % PartOfFile.MAX_SIZE == 0 ? size / PartOfFile.MAX_SIZE : size / PartOfFile.MAX_SIZE + 1);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public int getSizeOfPart(long part) {
        return (int) Math.min(size - part*PartOfFile.MAX_SIZE, PartOfFile.MAX_SIZE);
    }

    public int getNumOfParts() {
        return numOfParts;
    }

    @Override
    public String toString() {
        return "Name = \"" + name + "\"; id = " + id + "; size = " + size;
    }
}
