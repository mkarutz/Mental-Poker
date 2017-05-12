package au.edu.unimelb.mentalpoker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by azable on 12/05/17.
 */
public class RemoteReceiver extends Thread {

    private Remote remote;
    private IPacketReceiver packetReceiver;
    private DatagramSocket socket;
    private ConcurrentHashMap<Address, Queue<Proto.NetworkPacket>> incoming;
    private ConcurrentHashMap<Address, HashSet<Integer>> alreadyReceivedMessageIds;
    private boolean closed = false;

    public RemoteReceiver(DatagramSocket socket, IPacketReceiver packetReceiver, Remote remote) {
        this.packetReceiver = packetReceiver;
        this.remote = remote;
        this.socket = socket;
        this.incoming = new ConcurrentHashMap<>();
        this.alreadyReceivedMessageIds = new ConcurrentHashMap<>();
    }

    public synchronized Proto.NetworkMessage pop(Address source) {
        for (Map.Entry<Address, Queue<Proto.NetworkPacket>> addressQueuePair : this.incoming.entrySet()) {
            Address address = addressQueuePair.getKey();
            Queue<Proto.NetworkPacket> queue = addressQueuePair.getValue();

            if (!queue.isEmpty()) {
                source.setIp(address.getIp());
                source.setPort(address.getPort());

                return queue.remove().getNetworkMessage();
            }
        }
        return null;
    }

    public void run() {
        while (!this.closed) {
            try {
                processQueue();
            } catch (SocketTimeoutException e) {
                // do nothing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        this.closed = true;
    }

    private void processQueue() throws IOException {
        Address sourceAddress = new Address();
        Proto.NetworkPacket nextReceivedPacket = receivePacket(sourceAddress);
        if (nextReceivedPacket != null) {
            if (nextReceivedPacket.getNetworkMessageHeader().getAck() == true) {
                // Ack packet
                //System.out.println("ACK PACKET");
                this.remote.onReceivedPacketAck(sourceAddress, nextReceivedPacket);
            } else {
                // Regular packet
                //System.out.println("REGULAR PACKET");
                if (!packetAlreadyReceivedFromAddress(sourceAddress, nextReceivedPacket)) {
                    addPacketToIncomingAddressQueue(sourceAddress, nextReceivedPacket);
                    logPacketReceivedFromAddress(sourceAddress, nextReceivedPacket);
                }
                this.remote.sendPacketAck(sourceAddress, nextReceivedPacket);
            }
        }
    }

    private boolean packetAlreadyReceivedFromAddress(Address sourceAddress, Proto.NetworkPacket packet) {
        ensureAddressAlreadyReceivedMessageIdSetExists(sourceAddress);

        int messageId = packet.getNetworkMessageHeader().getMessageId();
        return this.alreadyReceivedMessageIds.get(sourceAddress).contains(messageId);
    }

    private void logPacketReceivedFromAddress(Address sourceAddress, Proto.NetworkPacket packet) {
        ensureAddressAlreadyReceivedMessageIdSetExists(sourceAddress);

        int messageId = packet.getNetworkMessageHeader().getMessageId();
        this.alreadyReceivedMessageIds.get(sourceAddress).add(messageId);
    }

    private void addPacketToIncomingAddressQueue(Address sourceAddress, Proto.NetworkPacket packet) {
        ensureIncomingAddressQueueExists(sourceAddress);
        this.incoming.get(sourceAddress).add(packet);
    }

    private void ensureIncomingAddressQueueExists(Address address) {
        if (!this.incoming.containsKey(address)) {
            this.incoming.put(address, new ArrayDeque<>());
        }
    }

    private void ensureAddressAlreadyReceivedMessageIdSetExists(Address address) {
        if (!this.alreadyReceivedMessageIds.containsKey(address)) {
            this.alreadyReceivedMessageIds.put(address, new HashSet<>());
        }
    }

    private Proto.NetworkPacket receivePacket(Address source) throws IOException {
        return this.packetReceiver.receivePacket(source);
    }
}
