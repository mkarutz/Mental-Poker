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
    boolean syncPhase = false;

    public PeerNetwork(Proto.GameStartedMessage gameInfo,int listenPort) {
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
        this.remote = new Remote(listenPort,this);
        this.remote.start();
        this.playersNotSynced = new HashSet<>(this.players.keySet());
        this.playersNotSynced.remove(this.localId);
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
        this.syncPhase = true;
        //System.out.println("Peer Network id ="+Thread.currentThread().getId());
        //resetSyncMap();
        Proto.NetworkMessage syncMessage =
                Proto.NetworkMessage.newBuilder()
                    .setType(Proto.NetworkMessage.Type.SYNC).build();
        while (!this.playersNotSynced.isEmpty()) {
            System.out.println("this not synced size ="+this.playersNotSynced.size());
            try {
                multicast(syncMessage,this.playersNotSynced);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Synced...");
    }

    private void resetSyncMap() {
        this.playersNotSynced.clear();
    }

    public void multicast(Proto.NetworkMessage message, HashSet<Integer> playerIDs){
        for (Integer playerId : playerIDs)
        {
            System.out.println("sending SYNC to:"+this.players.get(playerId));
            send(playerId, message);
        }
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

    public synchronized void onReceive(Address remote, Proto.NetworkMessage message) {
        if(this.syncPhase){
            if (message.getType() == Proto.NetworkMessage.Type.SYNC) {
                //System.out.println("Received SYNC ="+remote.port);
                Proto.NetworkMessage syncAckMessage =
                        Proto.NetworkMessage.newBuilder()
                                .setType(Proto.NetworkMessage.Type.SYNC_ACK).build();
                send(this.players.inverse().get(remote),syncAckMessage);
                return;
            }
            if (message.getType() == Proto.NetworkMessage.Type.SYNC_ACK) {
                System.out.println("Received SYNC_ACK ="+remote.port);
                this.playersNotSynced.remove(this.players.inverse().get(remote));
                return;
            }
        }
        if(message.getType() != Proto.NetworkMessage.Type.SYNC || message.getType() != Proto.NetworkMessage.Type.SYNC_ACK){
            int playerId = this.players.inverse().get(remote);
            queueForPlayer(playerId).add(message);
        }


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
