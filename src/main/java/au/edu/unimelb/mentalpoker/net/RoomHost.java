package au.edu.unimelb.mentalpoker.net;

import au.edu.unimelb.mentalpoker.Proto;

public class RoomHost implements Remote.Callbacks {
    private Remote remote;
    private GameTable gameTable;
    private Callbacks callbacks;
    private String ipAddress = null;

    public interface Callbacks {
        void onConnectionFailed(String message);
    }

    public RoomHost(Remote remote, Callbacks callbacks) {
        this.gameTable = new GameTable();
        this.remote = remote;
        this.remote.setListener(this);
        this.callbacks = callbacks;
    }

    public void onReceive(Address source, Proto.NetworkMessage message) {
        if (message.getType() == Proto.NetworkMessage.Type.JOIN_ROOM) {
            requestMyIpAddress(source);
            this.gameTable.addPlayerConnection(source);
            this.remote.send(source, Proto.NetworkMessage.newBuilder()
                    .setType(Proto.NetworkMessage.Type.JOIN_ROOM_ALLOWED).build());
        } else if (message.getType() == Proto.NetworkMessage.Type.PLAYER_READY) {
            handlePlayerReadyMessage(source);
        } else if (message.getType() == Proto.NetworkMessage.Type.REQUEST_IP_RESULT) {
            this.ipAddress = message.getRequestIpResultMessage().getIp();
        }
    }

    @Override
    public void onSendFailed(Address destination) {
        System.out.println("Error: Could not send message to: " + destination);
    }

    private void requestMyIpAddress(Address address) {
        if (!address.getIp().equals("127.0.0.1")) {
            this.remote.send(address,
                    Proto.NetworkMessage.newBuilder()
                            .setType(Proto.NetworkMessage.Type.REQUEST_IP).build());
        }
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
            String ip = address.getIp();
            if (this.ipAddress != null && ip.equals("127.0.0.1")) {
                ip = this.ipAddress;
            }
            Proto.PeerAddress.Builder peerAddress = Proto.PeerAddress.newBuilder()
                    .setHostname(ip)
                    .setPort(address.getPort());

            Proto.Player.Builder player = Proto.Player.newBuilder().setPlayerId(playerId).setAddress(peerAddress);
            gameStartedMessage.addPlayers(player);
            playerId++;
        }

        // Send game started message
        playerId = 1;
        for (Address address : this.gameTable.getPlayers()) {
            gameStartedMessage.setPlayerId(playerId);
            this.remote.send(address, Proto.NetworkMessage.newBuilder()
                    .setType(Proto.NetworkMessage.Type.GAME_STARTED)
                    .setGameStartedMessage(gameStartedMessage)
                    .build());
            playerId++;
        }

        //this.remote.close();
    }
}
