package au.edu.unimelb.mentalpoker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

/** This class provides an interface for reliable message delivery. */
public class Remote extends Thread implements Connection.Callbacks {
    private static final int RETRY_LIMIT = 10;

    private final ServerSocket serverSocket;
    private final HashMap<Address, Connection> outgoingConnections;
    private final int port;
    private Callbacks remoteListener;
    private boolean listening;
    private RemoteReceiver remoteReceiver;

    /** Interface for callbacks. */
    public interface Callbacks {
        void onReceive(Address source, Proto.NetworkMessage message);
    }

    public Remote(int port, Callbacks listener) {
        this.remoteReceiver = new RemoteReceiver();
        this.remoteReceiver.start();

        setListener(listener);
        this.listening = true;
        this.port = port;
        this.outgoingConnections = new HashMap<>();
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setListener(Callbacks listener) {
        this.remoteReceiver.setListener(listener);
        //this.remoteListener = listener;
    }

    /** Handles messages received on connections. Parses a NetworkMessage proto and calls onReceive callback. */
    @Override
    public synchronized void onReceive(Address source, byte[] message) {
        //System.out.println("aaaaaaaaaaaaaaaaaaaaaaggg");
        this.remoteReceiver.pushIncomingMessage(source, message);
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
        while (!result && retries < RETRY_LIMIT) {
            result = connect(destination);
            if (result) {
                result &= this.outgoingConnections.get(destination).Write(messageBytes);
            }
            retries++;
        }
        if (retries >= RETRY_LIMIT) {
            throw new IOException("Could not resolve remote host");
        }
    }

    private boolean connect(Address to) {
        try {
            Socket remoteSocket = new Socket(to.ip, to.port);
            Connection connection = new Connection(remoteSocket, this.port, this);
            new Thread(connection).start();
            this.outgoingConnections.put(to, connection);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void run() {
        while (this.listening) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                Connection clientConnection = new Connection(clientSocket, this.port, this);
                new Thread(clientConnection).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closeAllConnections();
    }

    public void finish() {
        this.listening = false;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void closeAllConnections() {
        ArrayList<Connection> connections = new ArrayList<>(this.outgoingConnections.values());
        for (Connection connection : connections) {
            connection.close();
        }
        this.outgoingConnections.clear();
    }
}
