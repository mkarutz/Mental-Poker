package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 12/05/17.
 */
public interface IPacketReceiver {
    Proto.NetworkPacket receivePacket(Address source) throws IOException;
}
