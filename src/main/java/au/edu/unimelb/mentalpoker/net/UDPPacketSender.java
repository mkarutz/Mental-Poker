package au.edu.unimelb.mentalpoker.net;

import au.edu.unimelb.mentalpoker.Proto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPPacketSender implements IPacketSender {
    private final DatagramSocket socket;

    public UDPPacketSender(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void sendPacket(Address destination, Proto.NetworkPacket packet) {
        byte[] messageBytes = packet.toByteArray();
        InetAddress address = null;
        try {
            address = InetAddress.getByName(destination.getIp());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DatagramPacket dpacket = new DatagramPacket(messageBytes, messageBytes.length, address, destination.getPort());
        try {
            this.socket.send(dpacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
