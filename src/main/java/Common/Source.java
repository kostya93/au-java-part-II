package Common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Class Source represents a source of file
 * like a pair of (IP, PORT)
 * and last update time
 */
public class Source {
    private final byte[] ip;
    private final int port;
    private long lastUpdate;

    public Source(byte[] ip, int port) {
        this.ip = ip;
        this.port = port;
        lastUpdate = System.currentTimeMillis();
    }

    public byte[] getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d:%d", ip[0], ip[1], ip[2], ip[3], port);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(ip)
                .append(port)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Source))
            return false;
        if (obj == this)
            return true;

        Source rhs = (Source) obj;
        return new EqualsBuilder()
                .append(ip, rhs.getIp())
                .append(port, rhs.getPort())
                .isEquals();
    }
}
