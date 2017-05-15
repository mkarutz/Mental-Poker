package au.edu.unimelb.mentalpoker;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;

/**
 * Created by azable on 10/05/17.
 */
public class PeerNetwork implements Remote.Callbacks {
    private static final long DEFAULT_RECV_TIMEOUT = 15000;

    private BiMap<Integer, Address> players;
    private int localId;
    private Remote remote;
    private HashMap<Integer, Queue<Proto.NetworkMessage>> playerMessages;
    private HashSet<Integer> playersNotSynced;

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
        this.playersNotSynced = new HashSet<>();
        initPlayerMessagesHashMap();
        synchronize();
    }

    private void initPlayerMessagesHashMap() {
        this.playerMessages = new HashMap<>();
        for (Integer playerId : this.players.keySet()) {
            createQueueForPlayer(playerId);
        }
    }

    public void synchronize() {
        resetSyncMap();
        Proto.NetworkMessage syncMessage =
                Proto.NetworkMessage.newBuilder()
                    .setType(Proto.NetworkMessage.Type.SYNC).build();

        while (!this.playersNotSynced.isEmpty()) {
            try {
                for (Integer playerId : this.playersNotSynced) {
                    send(playerId, syncMessage);
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvalidPlayerIdException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Synced...");
    }

    private void resetSyncMap() {
        this.playersNotSynced = new HashSet<>(this.players.keySet());
        this.playersNotSynced.remove(localId);
    }

    public void broadcast(Proto.NetworkMessage message) {
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
        long startTime = System.currentTimeMillis();
        while (queueForPlayer(playerId).isEmpty()) {
            if (System.currentTimeMillis() - startTime > PeerNetwork.DEFAULT_RECV_TIMEOUT) {
                throw new TimeoutException("Error: Timed out waiting for message from player " + playerId);
            }
        }
        return queueForPlayer(playerId).remove();
    }

    public int getLocalPlayerId() {
        return this.localId;
    }

    public int numPlayers() {
        return this.players.size();
    }

    public void send(int playerId, Proto.NetworkMessage message) throws InvalidPlayerIdException {
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

        if (message.getType() == Proto.NetworkMessage.Type.SYNC) {
            Proto.NetworkMessage syncAckMessage =
                    Proto.NetworkMessage.newBuilder()
                            .setType(Proto.NetworkMessage.Type.SYNC_ACK).build();
            try {
                send(playerId, syncAckMessage);
            } catch (InvalidPlayerIdException e) {
                e.printStackTrace();
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
        System.out.println("Error: Failed to send message to " + destination);
        System.exit(1);
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
