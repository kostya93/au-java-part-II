package Client;

import java.util.List;

/**
 * Class Source represents a source of file
 * like a pair of IP and PORT
 */
public class Source {
    private final List<Byte> ip;
    private final int port;

    public Source(List<Byte> ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public List<Byte> getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d:%d", ip.get(0), ip.get(1), ip.get(2), ip.get(3), port);
    }
}
