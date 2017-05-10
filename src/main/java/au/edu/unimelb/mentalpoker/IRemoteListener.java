package au.edu.unimelb.mentalpoker;

/**
 * Created by azable on 10/05/17.
 */
public interface IRemoteListener {
    void Receive(Address source, Proto.NetworkMessage message);
}
