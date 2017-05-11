package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 10/05/17.
 */
public class RoomHost implements IRemoteListener {
    private Remote remote;
    private GameTable gameTable;
    private Callbacks callbacks;

    public interface Callbacks {
        void onConnectionFailed(String message);
    }

    public RoomHost(int port, Callbacks callbacks) {
        this.gameTable = new GameTable();
        this.remote = new Remote(port, this);
        this.remote.start();
        this.callbacks = callbacks;
    }

    public void Receive(Address remote, Proto.NetworkMessage message) {
        if (message.getType() == Proto.NetworkMessage.Type.JOIN_ROOM) {
            this.gameTable.addPlayerConnection(remote);
        } else if (message.getType() == Proto.NetworkMessage.Type.PLAYER_READY) {
            this.gameTable.changeReadyState(remote, true);
        }

        if (this.gameTable.allPlayersReady()) {
            for (Address address : this.gameTable.getPlayers()) {
                try {
                    this.remote.Send(address, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.GAME_STARTED).build());
                } catch (IOException e) {
                    callbacks.onConnectionFailed("Failed to establish connections with players.");
                }
            }
        }
    }
}
