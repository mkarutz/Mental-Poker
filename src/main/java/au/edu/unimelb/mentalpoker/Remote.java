package au.edu.unimelb.mentalpoker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.HashMap;

/**
 * Created by azable on 9/05/17.
 */
public class Remote extends Thread implements IConnectionListener {
    private static final int RETRY_LIMIT = 10;

    private ServerSocket serverSocket;
    private HashMap<Address, Connection> outgoingConnections;
    private IRemoteListener remoteListener;
    private int port;

    public Remote(int port, IRemoteListener listener) {
        this.remoteListener = listener;
        this.port = port;
        this.outgoingConnections = new HashMap<>();
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /*public Remote(IRemoteListener listener) {
        this.remoteListener = listener;
        this.outgoingConnections = new HashMap<>();
        this.serverSocket = null;
    }*/

    public synchronized void Receive(Address source, byte[] message) {
        try {
            Proto.NetworkMessage networkMessage = Proto.NetworkMessage.parseFrom(message);
            this.remoteListener.Receive(source, networkMessage);
        } catch (Exception e) {
            System.out.println("Error: Invalid protobuf message");
        }
    }

    public synchronized void ConnectionClosed(Address source) {
        this.outgoingConnections.remove(source);
    }

    /*public void Broadcast(byte[] message) {
        for (Map.Entry<Address, Connection> connection : this.outgoingConnections.entrySet()) {
            System.out.println("Outgoing braodcast to " + connection.getKey().toString());
            Send(connection.getKey(), message);
        }
    }*/

    public void Send(Address destination, Proto.NetworkMessage message) throws IOException {
        byte[] messageBytes = message.toByteArray();

        if (!this.outgoingConnections.containsKey(destination)) {
            Connect(destination);
        }
        boolean result = this.outgoingConnections.get(destination).Write(messageBytes);
        int retries = 0;
        while (!result && retries++ < RETRY_LIMIT) {
            Connect(destination);
            result = this.outgoingConnections.get(destination).Write(messageBytes);
        }
        if (retries >= RETRY_LIMIT) {
            throw new IOException("Could not resolve remote host");
        }
    }

    private void Connect(Address to) {
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
