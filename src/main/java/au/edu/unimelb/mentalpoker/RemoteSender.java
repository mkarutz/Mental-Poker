package au.edu.unimelb.mentalpoker;

import com.google.protobuf.MapEntry;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by azable on 12/05/17.
 */
public class RemoteSender extends Thread {
    private static int nextMessageId = 0;

    private DatagramSocket socket;
    private ConcurrentHashMap<Address, Queue<Proto.NetworkPacket>> outgoing;
    private boolean closed = false;

    public RemoteSender(DatagramSocket socket) {
        this.socket = socket;
        this.outgoing = new ConcurrentHashMap<>();
    }

    public synchronized void push(Address destination, Proto.NetworkMessage message) {
        Proto.NetworkPacket packet = buildPacket(message);
        ensureOutgoingQueueForAddressExists(destination);
        this.outgoing.get(destination).add(packet);
    }

    public void run() {
        while (!this.closed) {
            processQueue();
        }
    }

    public void close() {
        this.closed = true;
    }

    private void processQueue() {
        for (Map.Entry<Address, Queue<Proto.NetworkPacket>> addressQueuePair : this.outgoing.entrySet()) {
            Address address = addressQueuePair.getKey();
            Queue<Proto.NetworkPacket> queue = addressQueuePair.getValue();

            if (!queue.isEmpty()) {
                sendPacket(address, queue.remove());
            }
        }
    }

    private void ensureOutgoingQueueForAddressExists(Address address) {
        if (!this.outgoing.containsKey(address)) {
            this.outgoing.put(address, new ArrayDeque<>());
        }
    }

    private void sendPacket(Address destination, Proto.NetworkPacket packetToSend) {
        byte[] messageBytes = packetToSend.toByteArray();
        InetAddress address = null;
        try {
            address = InetAddress.getByName(destination.ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, destination.port);
        //System.out.println("Sending packet to: " + packet.getPort() + " : " + messageBytes.length);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Proto.NetworkPacket buildPacket(Proto.NetworkMessage message) {
        int messageId = nextMessageId++;
        Proto.NetworkPacket packet = Proto.NetworkPacket.newBuilder()
                .setNetworkMessageHeader(
                        Proto.NetworkMessageHeader.newBuilder()
                                .setMessageId(messageId)
                                .setAck(false)
                                .build())
                .setNetworkMessage(message)
                .build();
        return packet;
    }
}
