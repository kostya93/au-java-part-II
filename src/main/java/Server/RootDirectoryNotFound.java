package Server;

/**
 * When the root directory not found
 */
public class RootDirectoryNotFound extends Exception {
    RootDirectoryNotFound(String s) {
        super(s);
    }
}
