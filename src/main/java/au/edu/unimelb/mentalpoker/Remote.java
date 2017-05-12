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

    private RemoteSender remoteSender;
    private RemoteReceiver remoteReceiver;

    /** Interface for callbacks. */
    public interface Callbacks {
        void onReceive(Address source, Proto.NetworkMessage message);
    }

    public Remote(int listeningPort, Callbacks listener) {
        this.listeningPort = listeningPort;
        this.listener = listener;
        try {
            this.socket = new DatagramSocket(this.listeningPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.running = true;

        this.remoteSender = new RemoteSender(this.socket);
        this.remoteSender.start();

        this.remoteReceiver = new RemoteReceiver(this.socket);
        this.remoteReceiver.start();
    }

    public void run() {
        while (this.running) {
            Address sourceAddress = new Address();
            Proto.NetworkMessage message = this.remoteReceiver.pop(sourceAddress);
            if (message != null) {
                this.listener.onReceive(sourceAddress, message);
            }
        }

        System.out.println("********************************");
        this.socket.close();
    }

    public void close() {
        System.out.println("*****************************************************");
        this.remoteSender.close();
        this.remoteReceiver.close();
        this.remoteReceiver.interrupt();
        try {
            this.remoteSender.join();
            this.remoteReceiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("********************************%%%%%%%%%%%%%");
        this.running = false;
    }

    /** Sends a message reliably to the given destination address */
    public void send(Address destination, Proto.NetworkMessage message) throws IOException {
        this.remoteSender.push(destination, message);
    }


}
