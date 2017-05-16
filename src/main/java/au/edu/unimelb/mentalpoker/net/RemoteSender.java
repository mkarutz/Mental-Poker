package au.edu.unimelb.mentalpoker.net;

import au.edu.unimelb.mentalpoker.Proto;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteSender extends Thread {
    private static final long RETRY_TIME = 300;
    private static final long CONNECTION_TIMEOUT = 30000;

    private static int nextMessageId = 0;

    private IPacketSender packetSender;
    private ConcurrentHashMap<Address, Queue<Proto.NetworkPacket>> outgoing;
    private ConcurrentHashMap<Address, AddressPacketDispatcher> packetDispatchers;
    private Queue<Address> timeouts;
    private long timeOutInterval;
    private boolean closed = false;

    private class AddressPacketDispatcher {
        private boolean slotFree;
        private Proto.NetworkPacket packetToDispatch;
        private Address destination;
        private long lastTryTime;
        private long startDispatchTime;
        private boolean timedOut;

        public AddressPacketDispatcher(Address destination) {
            this.destination = destination;
            reset();
        }

        public void tryAck(int ackMessageId) {
            if (!isSlotFree() && this.packetToDispatch != null) {
                if (ackMessageId == this.packetToDispatch.getNetworkMessageHeader().getMessageId()) {
                    this.slotFree = true;
                    this.lastTryTime = 0;
                    this.timedOut = false;
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
                this.lastTryTime = 0;
                this.startDispatchTime = System.currentTimeMillis();
            }
        }

        public boolean tryDispatch() {
            if (this.timedOut == true) {
                return true;
            }
            if (!isSlotFree() && this.packetToDispatch != null && lastTryMilliseconds() >= RemoteSender.RETRY_TIME) {
                sendPacket(this.destination, this.packetToDispatch);
                this.lastTryTime = System.currentTimeMillis();
                long retryTimeElapsed = System.currentTimeMillis() - this.startDispatchTime;
                if (retryTimeElapsed > timeOutInterval && this.timedOut == false) {
                    this.timedOut = true;
                    return false; // Return time-out result once
                }
            }
            return true;
        }

        private long lastTryMilliseconds() {
            return System.currentTimeMillis() - this.lastTryTime;
        }

        private void reset() {
            this.timedOut = false;
            this.slotFree = true;
            this.packetToDispatch = null;
            this.lastTryTime = 0;
        }
    }

    public RemoteSender(IPacketSender packetSender) {
        this.packetSender = packetSender;
        this.outgoing = new ConcurrentHashMap<>();
        this.packetDispatchers = new ConcurrentHashMap<>();
        this.timeouts = new ArrayDeque<>();
        this.timeOutInterval = CONNECTION_TIMEOUT;
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

    public void setTimeOutInterval(long timeOutInterval) {
        this.timeOutInterval = timeOutInterval;
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
            boolean timedOut = !dispatcherEntry.getValue().tryDispatch();
            if (timedOut) {
                this.timeouts.add(dispatcherEntry.getKey());
            }
        }
    }

    public Address checkTimeOut() {
        if (this.timeouts.size() > 0) {
            return this.timeouts.remove();
        }
        return null;
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
