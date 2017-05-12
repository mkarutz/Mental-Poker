package au.edu.unimelb.mentalpoker;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by azable on 10/05/17.
 */
public class Address {
    public String ip;
    public int port;

    public Address() {

    }

    public Address(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Address(Socket socket) {
        this.ip = socket.getInetAddress().getHostAddress();
        this.port = socket.getPort();
    }

    @Override
    public String toString() {
        return "Address{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (port != address.port) return false;
        return ip != null ? ip.equals(address.ip) : address.ip == null;

    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
