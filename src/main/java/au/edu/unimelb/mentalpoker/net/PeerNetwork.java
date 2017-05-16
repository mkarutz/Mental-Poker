package au.edu.unimelb.mentalpoker.net;

import au.edu.unimelb.mentalpoker.Proto;
import au.edu.unimelb.mentalpoker.exceptions.InvalidPlayerIdException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;

public class PeerNetwork extends Thread implements Remote.Callbacks {
    private static final long PING_INTERVAL = 2000;
    private static final long DEFAULT_TIMEOUT = 30000;

    private PeerNetwork.Callbacks callbacks;
    private BiMap<Integer, Address> players;
    private int localId;
    private Remote remote;
    private HashMap<Integer, Queue<Proto.NetworkMessage>> playerMessages;
    private HashMap<Integer, Long> lastPingTime;
    private HashSet<Integer> playersNotSynced;

    /** Interface for callbacks. */
    public interface Callbacks {
        void onTimeOut(int playerId);
    }

    public PeerNetwork(Proto.GameStartedMessage gameInfo, Remote remote) {
        this.players = HashBiMap.create();
        for (Proto.Player peer : gameInfo.getPlayersList()) {
            Proto.PeerAddress peerAddress = peer.getAddress();
            this.players.put(
                    peer.getPlayerId(),
                    new Address(
                            peerAddress.getHostname(),
                            peerAddress.getPort()));
        }
        this.localId = gameInfo.getPlayerId();
        this.remote = remote;
        this.remote.setListener(this);
        this.remote.setTimeOutInterval(30000);
        this.lastPingTime = new HashMap<>();
        this.playersNotSynced = new HashSet<>();
        initPlayerMessagesHashMap();
        synchronize();
    }

    public void setListener(PeerNetwork.Callbacks listener) {
        this.callbacks = listener;
    }

    @Override
    public void run() {
        for (Integer playerId : this.players.keySet()) {
            if (playerId != getLocalPlayerId()) {
                this.lastPingTime.put(playerId, System.currentTimeMillis());
            }
        }
        while (true) {
            pingPeers();
            checkPeers();
            try {
                Thread.sleep(PeerNetwork.PING_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void verifyPlayerConnection(int playerId) throws TimeoutException {
        if (this.lastPingTime.containsKey(playerId))
            if (System.currentTimeMillis() - this.lastPingTime.get(playerId) > PeerNetwork.DEFAULT_TIMEOUT)
                throw new TimeoutException("\nError: Timed out reaching player " + playerId);
    }

    private void pingPeers() {
        Proto.NetworkMessage message = Proto.NetworkMessage.newBuilder()
                .setType(Proto.NetworkMessage.Type.PING)
                .build();
        try {
            broadcast(message);
        } catch (TimeoutException e) {
            // Internal method, no need to handle
        }
    }

    private void checkPeers() {
        for (HashMap.Entry<Integer, Long> entry : this.lastPingTime.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > PeerNetwork.DEFAULT_TIMEOUT) {
                if (this.callbacks != null) {
                    this.callbacks.onTimeOut(entry.getKey());
                }
            }
        }
    }

    private void initPlayerMessagesHashMap() {
        this.playerMessages = new HashMap<>();
        for (Integer playerId : this.players.keySet()) {
            createQueueForPlayer(playerId);
        }
    }

    private void synchronize() {
        resetSyncMap();
        Proto.NetworkMessage syncMessage =
                Proto.NetworkMessage.newBuilder()
                    .setType(Proto.NetworkMessage.Type.SYNC).build();

        long startTime = System.currentTimeMillis();
        while (!this.playersNotSynced.isEmpty() && System.currentTimeMillis() - startTime < DEFAULT_TIMEOUT) {
            try {
                for (Integer playerId : this.playersNotSynced) {
                    send(playerId, syncMessage);
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvalidPlayerIdException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                // Internal method, no need to handle
            }
        }
    }

    private void resetSyncMap() {
        this.playersNotSynced = new HashSet<>(this.players.keySet());
        this.playersNotSynced.remove(localId);
    }

    public void broadcast(Proto.NetworkMessage message) throws TimeoutException {
        for (Integer playerId : this.players.keySet()) {
            if (playerId != getLocalPlayerId()) {
                try {
                    send(playerId, message);
                } catch (InvalidPlayerIdException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Proto.NetworkMessage receive(int playerId)
            throws InvalidPlayerIdException, TimeoutException
    {
        if (!this.players.containsKey(playerId)) {
            throw new InvalidPlayerIdException();
        }
        while (queueForPlayer(playerId).isEmpty()) {
            verifyPlayerConnection(playerId);
        }
        return queueForPlayer(playerId).remove();
    }

    public int getLocalPlayerId() {
        return this.localId;
    }

    public int numPlayers() {
        return this.players.size();
    }

    public void send(int playerId, Proto.NetworkMessage message) throws
            InvalidPlayerIdException, TimeoutException
    {
        verifyPlayerConnection(playerId);
        this.remote.send(lookupPlayerAddress(playerId), message);
    }

    private Address lookupPlayerAddress(int playerId) throws InvalidPlayerIdException {
        if (this.players.containsKey(playerId)) {
            return this.players.get(playerId);
        } else {
            throw new InvalidPlayerIdException();
        }
    }

    private int lookupPlayerId(Address address) {
        return this.players.inverse().get(address);
    }

    public void onReceive(Address remote, Proto.NetworkMessage message) {
        int playerId = lookupPlayerId(remote);

        if (message.getType() == Proto.NetworkMessage.Type.PING) {
            this.lastPingTime.put(playerId, System.currentTimeMillis());
            //System.out.println("PING " + playerId);
            return;
        }

        if (message.getType() == Proto.NetworkMessage.Type.SYNC) {
            Proto.NetworkMessage syncAckMessage =
                    Proto.NetworkMessage.newBuilder()
                            .setType(Proto.NetworkMessage.Type.SYNC_ACK).build();
            try {
                send(playerId, syncAckMessage);
            } catch (InvalidPlayerIdException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                // Internal method, no need to handle
            }
            return;
        }

        if (message.getType() == Proto.NetworkMessage.Type.SYNC_ACK) {
            this.playersNotSynced.remove(playerId);
            return;
        }

        queueForPlayer(playerId).add(message);
    }

    @Override
    public void onSendFailed(Address destination) {
        this.lastPingTime.put(lookupPlayerId(destination), 0L);
    }

    private Queue<Proto.NetworkMessage> queueForPlayer(int playerId) {
        return this.playerMessages.get(playerId);
    }

    private void createQueueForPlayer(int playerId) {
        if (!this.playerMessages.containsKey(playerId)) {
            this.playerMessages.put(playerId, new ConcurrentLinkedDeque<>());
        }
    }

    public int getNumPlayers() {
        return players.size();
    }
}
