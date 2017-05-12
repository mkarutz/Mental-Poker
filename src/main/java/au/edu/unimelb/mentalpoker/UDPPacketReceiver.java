package au.edu.unimelb.mentalpoker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

/**
 * Created by azable on 12/05/17.
 */
public class UDPPacketReceiver implements IPacketReceiver {

    private DatagramSocket socket;

    public UDPPacketReceiver(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public Proto.NetworkPacket receivePacket(Address source) throws IOException {
        byte[] buffer = new byte[65000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.socket.receive(packet);

        byte[] packetBytes = Arrays.copyOf(packet.getData(), packet.getLength());
        try {
            source.setIp(packet.getAddress().getHostName());
            source.setPort(packet.getPort());

            return Proto.NetworkPacket.parseFrom(packetBytes);

        } catch (InvalidProtocolBufferException e) {
            throw new IOException("Invalid message");
        }
    }
}
