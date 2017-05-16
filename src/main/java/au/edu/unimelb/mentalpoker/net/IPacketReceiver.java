package au.edu.unimelb.mentalpoker.net;

import au.edu.unimelb.mentalpoker.Proto;

import java.io.IOException;

public interface IPacketReceiver {
    Proto.NetworkPacket receivePacket(Address source) throws IOException;
}
