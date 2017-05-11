package au.edu.unimelb.mentalpoker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/** This class provides an interface for reliable message delivery. */
public class Remote extends Thread {

    private int listeningPort;
    private Callbacks listener;
    private DatagramSocket socket;
    private boolean running;

    /** Interface for callbacks. */
    public interface Callbacks {
        void onReceive(Address source, Proto.NetworkMessage message);
    }

    public Remote(int listeningPort, Callbacks listener) {
        this.listeningPort = listeningPort;
        this.listener = listener;
        System.out.println("My listening port is: " + this.listeningPort);
        try {
            this.socket = new DatagramSocket(this.listeningPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.running = true;
    }

    public void run() {
        while (this.running) {
            byte[] buffer = new byte[65000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] messageBytes = Arrays.copyOf(packet.getData(), packet.getLength());
            //System.out.println("Receiving packet from " + packet.getPort() + " : " + messageBytes.length);
            try {
                Proto.NetworkMessage protoMessage = Proto.NetworkMessage.parseFrom(messageBytes);
                //System.out.println(protoMessage.toString());

                Address sourceAddress = new Address(packet.getAddress().getHostName(), packet.getPort());
                this.listener.onReceive(sourceAddress, protoMessage);

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        this.socket.close();
    }

    public void close() {
        this.running = false;
    }

    /** Sends a message reliably to the given destination address */
    public void send(Address destination, Proto.NetworkMessage message) throws IOException {
        byte[] messageBytes = message.toByteArray();
        InetAddress address = InetAddress.getByName(destination.ip);
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, destination.port);
        //System.out.println("Sending packet to: " + packet.getPort() + " : " + messageBytes.length);
        this.socket.send(packet);
    }
}
