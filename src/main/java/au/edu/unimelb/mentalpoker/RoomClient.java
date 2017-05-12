package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 10/05/17.
 */
public class RoomClient implements Remote.Callbacks {
    private Remote remote;
    private Address hostAddress;
    private boolean gameStarted;
    private Proto.GameStartedMessage gameStartedMessage;

    public RoomClient(Address hostAddress, Remote remote) throws IOException {
        this.remote = remote;
        this.remote.setListener(this);
        this.gameStarted = false;

        this.hostAddress = hostAddress;

        Proto.NetworkMessage.Builder messageBuilder = Proto.NetworkMessage.newBuilder();
        messageBuilder.setType(Proto.NetworkMessage.Type.JOIN_ROOM);
        this.remote.send(this.hostAddress, messageBuilder.build());
    }

    public void Ready() throws IOException {
        this.remote.send(this.hostAddress, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.PLAYER_READY).build());
    }

    public void onReceive(Address remote, Proto.NetworkMessage message) {
        if (message.getType() == Proto.NetworkMessage.Type.GAME_STARTED) {
            this.gameStarted = true;
            this.gameStartedMessage = message.getGameStartedMessage();

        }
    }

    public boolean isGameStarted() {
        return this.gameStarted;
    }

    public Proto.GameStartedMessage getGameStartedMessage() {
        return this.gameStartedMessage;
    }
}
