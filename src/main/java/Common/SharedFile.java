package Common;

/**
 * Class SharedFile represents a shared file;
 *  - name: name of this file
 *  - id: id of this file
 *  - size: size of this file (in bytes)
 */
public class SharedFile {
    private final String name;
    private final int id;
    private final long size;

    public SharedFile(String name, int id, long size) {
        this.name = name;
        this.id = id;
        this.size = size;
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
}
