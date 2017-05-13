package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 10/05/17.
 */
public class RoomHost implements Remote.Callbacks {
    private Remote remote;
    private GameTable gameTable;
    private Callbacks callbacks;
    private String ipAddress;

    public interface Callbacks {
        void onConnectionFailed(String message);
    }

    public RoomHost(int port, Callbacks callbacks) {
        this.gameTable = new GameTable();
        this.remote = new Remote(port, this);
        this.remote.start();
        this.callbacks = callbacks;
    }

    public void onReceive(Address source, Proto.NetworkMessage message) {
        if (message.getType() == Proto.NetworkMessage.Type.JOIN_ROOM) {
            requestMyIpAddress(source);
            this.gameTable.addPlayerConnection(source);
            try {
                this.remote.send(source, Proto.NetworkMessage.newBuilder()
                        .setType(Proto.NetworkMessage.Type.JOIN_ROOM_ALLOWED).build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (message.getType() == Proto.NetworkMessage.Type.PLAYER_READY) {
            handlePlayerReadyMessage(source);
        } else if (message.getType() == Proto.NetworkMessage.Type.REQUEST_IP_RESULT) {
            this.ipAddress = message.getRequestIpResultMessage().getIp();
            System.out.println("My IP according to someone else is: " + this.ipAddress);
        }
    }

    private void requestMyIpAddress(Address address) {
        //if (!address.getIp().equals("127.0.0.1")) {
            try {
                this.remote.send(address,
                        Proto.NetworkMessage.newBuilder()
                                .setType(Proto.NetworkMessage.Type.REQUEST_IP).build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        //}
    }

    private void handlePlayerReadyMessage(Address remote) {
        gameTable.changeReadyState(remote, true);
        maybeStartGame();
    }

    private void maybeStartGame() {
        if (!gameTable.allPlayersReady()) {
            return;
        }

        // Build game started message
        Proto.GameStartedMessage.Builder gameStartedMessage = Proto.GameStartedMessage.newBuilder();
        int playerId = 1;
        for (Address address : this.gameTable.getPlayers()) {
            Proto.PeerAddress.Builder peerAddress = Proto.PeerAddress.newBuilder()
                    .setHostname(address.getIp())
                    .setPort(address.getPort());

            Proto.Player.Builder player = Proto.Player.newBuilder().setPlayerId(playerId).setAddress(peerAddress);
            gameStartedMessage.addPlayers(player);
            playerId++;
        }

        // Send game started message
        playerId = 1;
        for (Address address : this.gameTable.getPlayers()) {
            gameStartedMessage.setPlayerId(playerId);
            try {
                this.remote.send(address, Proto.NetworkMessage.newBuilder()
                        .setType(Proto.NetworkMessage.Type.GAME_STARTED)
                        .setGameStartedMessage(gameStartedMessage)
                        .build());
            } catch (IOException e) {
                callbacks.onConnectionFailed("Failed to establish connections with players.");
            }
            playerId++;
        }

        //this.remote.close();
    }
}
