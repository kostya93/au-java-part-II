package Common;

/**
 * Created by kostya on 06.11.2016.
 */
public class SerializationException extends Exception {
    public SerializationException(String s) {
        super(s);
    }
    public SerializationException(String s, Exception e) {
        super(s, e);
    }
}
