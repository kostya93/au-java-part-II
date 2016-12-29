package Client;

/**
 * Created by kostya on 14.10.2016.
 */
public class MyFile {
    private final String name;
    private final boolean isDir;

    public String getName() {
        return name;
    }

    public boolean isDir() {
        return isDir;
    }

    public MyFile(String name, boolean isDir) {
        this.name = name;
        this.isDir = isDir;
    }

    @Override
    public String toString() {
        return "name = " + name + "; isDir = " + isDir;
    }
}
