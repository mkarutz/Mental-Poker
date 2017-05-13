package au.edu.unimelb.mentalpoker;

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

    private IPacketSender packetSender;
    private ConcurrentHashMap<Address, Queue<Proto.NetworkPacket>> outgoing;
    private ConcurrentHashMap<Address, AddressPacketDispatcher> packetDispatchers;
    private boolean closed = false;

    private class AddressPacketDispatcher {
        private boolean slotFree;
        private Proto.NetworkPacket packetToDispatch;
        private Address destination;
        private long lastTryTime;

        public AddressPacketDispatcher(Address destination) {
            this.destination = destination;
            reset();
        }

        public void tryAck(int ackMessageId) {
            if (!isSlotFree() && this.packetToDispatch != null) {
                if (ackMessageId == this.packetToDispatch.getNetworkMessageHeader().getMessageId()) {
                    this.slotFree = true;
                    this.lastTryTime = 0;
                }
            }
        }

        public boolean isSlotFree() {
            return this.slotFree;
        }

        public void setNextToDispatch(Proto.NetworkPacket packet) {
            if (isSlotFree()) {
                this.packetToDispatch = packet;
                this.slotFree = false;
            }
        }

        public void tryDispatch() {
            if (!isSlotFree() && this.packetToDispatch != null && lastTryMilliseconds() >= 100) {
                sendPacket(this.destination, this.packetToDispatch);
                this.lastTryTime = System.currentTimeMillis();
            }
        }

        private long lastTryMilliseconds() {
            return System.currentTimeMillis() - this.lastTryTime;
        }

        private void reset() {
            this.slotFree = true;
            this.packetToDispatch = null;
            this.lastTryTime = 0;
        }
    }

    public RemoteSender(IPacketSender packetSender) {
        this.packetSender = packetSender;
        this.outgoing = new ConcurrentHashMap<>();
        this.packetDispatchers = new ConcurrentHashMap<>();
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
        // Try moving messages to dispatchers
        for (Map.Entry<Address, Queue<Proto.NetworkPacket>> addressQueuePair : this.outgoing.entrySet()) {
            Address address = addressQueuePair.getKey();
            Queue<Proto.NetworkPacket> queue = addressQueuePair.getValue();

            if (!queue.isEmpty()) {
                // Push into dispatcher slot if it is empty (i.e. last message successfully sent and acked)
                ensurePacketDispatcherForAddressExists(address);
                AddressPacketDispatcher dispatcher = this.packetDispatchers.get(address);
                if (dispatcher.isSlotFree()) {
                    dispatcher.setNextToDispatch(queue.remove());
                }
            }
        }

        // Try executing dispatchers
        for (Map.Entry<Address, AddressPacketDispatcher> dispatcherEntry : this.packetDispatchers.entrySet()) {
            dispatcherEntry.getValue().tryDispatch();
        }
    }

    private void ensureOutgoingQueueForAddressExists(Address address) {
        if (!this.outgoing.containsKey(address)) {
            this.outgoing.put(address, new ArrayDeque<>());
        }
    }

    private void ensurePacketDispatcherForAddressExists(Address address) {
        if (!this.packetDispatchers.containsKey(address)) {
            this.packetDispatchers.put(address, new AddressPacketDispatcher(address));
        }
    }

    public synchronized void sendPacketAck(Address sourceAddress, Proto.NetworkPacket packet) {
        Proto.NetworkPacket ackPacket = Proto.NetworkPacket.newBuilder()
                .setNetworkMessageHeader(
                        Proto.NetworkMessageHeader.newBuilder()
                                .setMessageId(packet.getNetworkMessageHeader().getMessageId())
                                .setAck(true))
                .build();

        sendPacket(sourceAddress, ackPacket);
    }

    public synchronized void ackMessageIdForAddress(Address sourceAddress, int messageId) {
        ensurePacketDispatcherForAddressExists(sourceAddress);
        this.packetDispatchers.get(sourceAddress).tryAck(messageId);
    }

    private synchronized void sendPacket(Address destination, Proto.NetworkPacket packetToSend) {
        this.packetSender.sendPacket(destination, packetToSend);
    }

    private Proto.NetworkPacket buildPacket(Proto.NetworkMessage message) {
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
