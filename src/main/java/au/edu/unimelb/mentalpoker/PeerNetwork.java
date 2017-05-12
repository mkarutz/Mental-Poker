package au.edu.unimelb.mentalpoker;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by azable on 10/05/17.
 */
public class PeerNetwork implements Remote.Callbacks {

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
        //this.remote = new Remote(listeningPort, this);
        //this.remote.start();
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
            if (playerId != localId()) {
                send(playerId, message);
            }
        }
    }

    public Proto.NetworkMessage receive(int playerId) {
        while (queueForPlayer(playerId).isEmpty());
        return queueForPlayer(playerId).remove();
    }

    public int localId() {
        return this.localId;
    }

    public int numPlayers() {
        return this.players.size();
    }

    public void send(int playerId, Proto.NetworkMessage message) {
        try {
            this.remote.send(this.players.get(playerId), message);
        } catch (IOException e) {
            System.out.println("Error: Could not send message to player " + playerId);
        }
    }

    private Address lookupPlayerAddress(int playerId) {
        return this.players.get(playerId);
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
            send(playerId, syncAckMessage);
            return;
        }

        if (message.getType() == Proto.NetworkMessage.Type.SYNC_ACK) {
            this.playersNotSynced.remove(playerId);
            return;
        }

        queueForPlayer(playerId).add(message);
    }

    private Queue<Proto.NetworkMessage> queueForPlayer(int playerId) {
        return this.playerMessages.get(playerId);
    }

    private void createQueueForPlayer(int playerId) {
        if (!this.playerMessages.containsKey(playerId)) {
            this.playerMessages.put(playerId, new ConcurrentLinkedDeque<>());
        }
    }
}
