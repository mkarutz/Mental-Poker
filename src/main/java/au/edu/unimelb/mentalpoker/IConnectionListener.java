package au.edu.unimelb.mentalpoker;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by azable on 10/05/17.
 */
public interface IConnectionListener {
    void Receive(Address source, byte[] message);
    void ConnectionClosed(Address source);
}
