package au.edu.unimelb.mentalpoker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;

/** This class provides an interface for reliable message delivery. */
public class Remote extends Thread {

    private int listeningPort;
    private Callbacks listener;
    private DatagramSocket socket;
    private boolean running;
    private long timeOutInterval;

    private RemoteSender remoteSender;
    private RemoteReceiver remoteReceiver;

    /** Interface for callbacks. */
    public interface Callbacks {
        void onReceive(Address source, Proto.NetworkMessage message);
        void onSendFailed(Address destination);
    }

    public Remote(int listeningPort, Callbacks listener) {
        this.listeningPort = listeningPort;
        this.listener = listener;
        try {
            this.socket = new DatagramSocket(this.listeningPort);
            this.socket.setSoTimeout(100);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.running = true;
    }

    @Override
    public void start() {
        this.remoteSender = new RemoteSender(new UDPPacketSender(this.socket));
        this.remoteSender.setDaemon(true);
        this.remoteSender.start();

        this.remoteReceiver = new RemoteReceiver(new UDPPacketReceiver(this.socket), this);
        this.remoteReceiver.setDaemon(true);
        this.remoteReceiver.start();

        super.start();
    }

    public void setListener(Callbacks listener) {
        this.listener = listener;
    }

    public void setTimeOutInterval(long timeOutInterval) {
        this.remoteSender.setTimeOutInterval(timeOutInterval);
    }

    public void run() {
        while (this.running) {
            // Check for incoming messages
            Address sourceAddress = new Address();
            Proto.NetworkMessage message = this.remoteReceiver.pop(sourceAddress);
            if (message != null) {
                if (this.listener != null) {
                    this.listener.onReceive(sourceAddress, message);
                }
            }
            // Check for timeouts
            Address possibleTimeoutAddress = this.remoteSender.checkTimeOut();
            if (possibleTimeoutAddress != null) {
                this.listener.onSendFailed(possibleTimeoutAddress);
            }
        }
    }

    public void close() {
        this.remoteSender.close();
        this.remoteReceiver.close();

        try {
            this.remoteSender.join();
            this.remoteReceiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.socket.close();
        this.running = false;
    }

    /** Sends a message reliably to the given destination address */
    public void send(Address destination, Proto.NetworkMessage message) {
        this.remoteSender.push(destination, message);
    }

    public synchronized void sendPacketAck(Address sourceAddress, Proto.NetworkPacket packet) {
        this.remoteSender.sendPacketAck(sourceAddress, packet);
    }

    public synchronized void onReceivedPacketAck(Address sourceAddress, Proto.NetworkPacket ackPacket) {
        this.remoteSender.ackMessageIdForAddress(sourceAddress, ackPacket.getNetworkMessageHeader().getMessageId());
    }
}
