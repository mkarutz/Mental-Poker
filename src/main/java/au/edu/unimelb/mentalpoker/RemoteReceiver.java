package au.edu.unimelb.mentalpoker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by azable on 12/05/17.
 */
public class RemoteReceiver extends Thread {

    private DatagramSocket socket;
    private ConcurrentHashMap<Address, Queue<Proto.NetworkPacket>> incoming;
    private boolean closed = false;

    public RemoteReceiver(DatagramSocket socket) {
        this.socket = socket;
        this.incoming = new ConcurrentHashMap<>();
    }

    public synchronized Proto.NetworkMessage pop(Address source) {
        for (Map.Entry<Address, Queue<Proto.NetworkPacket>> addressQueuePair : this.incoming.entrySet()) {
            Address address = addressQueuePair.getKey();
            Queue<Proto.NetworkPacket> queue = addressQueuePair.getValue();

            if (!queue.isEmpty()) {
                source.ip = address.ip;
                source.port = address.port;
                return queue.remove().getNetworkMessage();
            }
        }
        return null;
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
        Address sourceAddress = new Address();
        Proto.NetworkPacket nextReceivedPacket = receivePacket(sourceAddress);
        ensureIncomingQueueForAddressExists(sourceAddress);
        this.incoming.get(sourceAddress).add(nextReceivedPacket);
    }

    private void ensureIncomingQueueForAddressExists(Address address) {
        if (!this.incoming.containsKey(address)) {
            this.incoming.put(address, new ArrayDeque<>());
        }
    }

    private Proto.NetworkPacket receivePacket(Address source) {
        byte[] buffer = new byte[65000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            this.socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] packetBytes = Arrays.copyOf(packet.getData(), packet.getLength());
        //System.out.println("Receiving packet from " + packet.getPort() + " : " + messageBytes.length);
        try {
            //Proto.NetworkPacket receivedPacket =

            //Proto.NetworkMessage protoMessage = readAndAckPacket(receivedPacket);
            //System.out.println(protoMessage.toString());
            source.ip = packet.getAddress().getHostName();
            source.port = packet.getPort();

            return Proto.NetworkPacket.parseFrom(packetBytes);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    //public Proto.NetworkMessage readMessageFromPacket(Proto.NetworkPacket packet) {
    //    return packet.getNetworkMessage();
    //}
}
