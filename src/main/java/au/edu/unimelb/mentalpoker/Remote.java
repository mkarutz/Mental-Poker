package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

/** This class provides an interface for reliable message delivery. */
public class Remote extends Thread implements Connection.Callbacks {
    private static final int RETRY_LIMIT = 10;

    private final ServerSocket serverSocket;
    private final HashMap<Address, Connection> outgoingConnections;
    private final Callbacks remoteListener;
    private final int port;

    /** Interface for callbacks. */
    public interface Callbacks {
        void onReceive(Address source, Proto.NetworkMessage message);
    }

    public Remote(int port, Callbacks listener) {
        this.remoteListener = listener;
        this.port = port;
        this.outgoingConnections = new HashMap<>();
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    /** Handles messages received on connections. Parses a NetworkMessage proto and calls onReceive callback. */
    @Override
    public synchronized void onReceive(Address source, byte[] message) {
        try {
            Proto.NetworkMessage networkMessage = Proto.NetworkMessage.parseFrom(message);
            this.remoteListener.onReceive(source, networkMessage);
        } catch (Exception e) {
            System.out.println("Error: Invalid protobuf message");
        }
    }

    @Override
    public synchronized void onConnectionClosed(Address source) {
        this.outgoingConnections.remove(source);
    }

    /** Sends a message reliably to the given destination address */
    public void send(Address destination, Proto.NetworkMessage message) throws IOException {
        byte[] messageBytes = message.toByteArray();

        if (!this.outgoingConnections.containsKey(destination)) {
            connect(destination);
        }
        boolean result = this.outgoingConnections.get(destination).Write(messageBytes);
        int retries = 0;
        while (!result && retries++ < RETRY_LIMIT) {
            connect(destination);
            result = this.outgoingConnections.get(destination).Write(messageBytes);
        }
        if (retries >= RETRY_LIMIT) {
            throw new IOException("Could not resolve remote host");
        }
    }

    private void connect(Address to) {
        try {
            Socket remoteSocket = new Socket(to.ip, to.port);
            Connection connection = new Connection(remoteSocket, this.port, this);
            new Thread(connection).start();
            this.outgoingConnections.put(to, connection);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void run() {
        while (true) {
            if (this.serverSocket != null) {
                try {
                    Socket clientSocket = this.serverSocket.accept();
                    Connection clientConnection = new Connection(clientSocket, this.port, this);
                    new Thread(clientConnection).start();
                    //this.outgoingConnections.put(new Address(clientSocket), clientConnection);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }
}
