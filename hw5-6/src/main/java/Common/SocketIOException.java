package Common;

import java.io.IOException;

/**
 * When the socket IOException
 */
public class SocketIOException extends IOException {
    public SocketIOException(String s) {
        super(s);
    }
}