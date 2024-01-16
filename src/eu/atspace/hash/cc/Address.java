package eu.atspace.hash.cc;

import java.io.Serializable;
import java.util.Arrays;


/**
 * IPv4 address, little endian format
 */
@SuppressWarnings("serial")
public class Address implements Serializable {
    private byte addr[] = new byte[6];

    public Address(int id, short port) {
        addr[0] = (byte) (id & 0xFF);
        id >>>= 8;
        addr[1] = (byte) (id & 0xFF);
        id >>>= 8;
        addr[2] = (byte) (id & 0xFF);
        id >>>= 8;
        addr[3] = (byte) (id & 0xFF);

        addr[4] = (byte) (port & 0xFF);
        port >>>= 8;
        addr[5] = (byte) (port & 0xFF);
    }

    public int getId() {
        int id = (addr[3] & 0xFF);
        id <<= 8;
        id |= (addr[2] & 0xFF);
        id <<= 8;
        id |= (addr[1] & 0xFF);
        id <<= 8;
        id |= (addr[0] & 0xFF);

        return id;
    }

    public short getPort() {
        short port = (short) (addr[5] & 0xFF);
        port <<= 8;
        port |= (short) (addr[4] & 0xFF);

        return port;
    }

    public String toString() {
        return String.format("%d.%d.%d.%d:%d", addr[0], addr[1], addr[2], addr[3], getPort());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(addr);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (!Arrays.equals(addr, other.addr))
            return false;
        return true;
    }

}
