package au.edu.unimelb.mentalpoker.net;

import au.edu.unimelb.mentalpoker.Proto;

public interface IPacketSender {
    void sendPacket(Address destination, Proto.NetworkPacket packet);
}
