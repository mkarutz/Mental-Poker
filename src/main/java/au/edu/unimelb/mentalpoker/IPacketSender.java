package au.edu.unimelb.mentalpoker;

/**
 * Created by azable on 12/05/17.
 */
public interface IPacketSender {
    void sendPacket(Address destination, Proto.NetworkPacket packet);
}
