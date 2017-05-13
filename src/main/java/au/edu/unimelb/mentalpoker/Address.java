package au.edu.unimelb.mentalpoker;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by azable on 10/05/17.
 */
public class Address {
    private String ip;
    private int port;

    public Address() {}

    public Address(String ip, int port) {
        setIp(ip);
        setPort(port);
    }

    public Address(Socket socket) {
        this.ip = socket.getInetAddress().getHostAddress();
        this.port = socket.getPort();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        String ipResolved = ip;
        try {
            InetAddress address = InetAddress.getByName(ip);
            ipResolved = address.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.ip = ipResolved;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
