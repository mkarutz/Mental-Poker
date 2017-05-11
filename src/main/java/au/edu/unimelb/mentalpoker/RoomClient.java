package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 10/05/17.
 */
public class RoomClient implements Remote.Callbacks {
    private Remote remote;
    private Address hostAddress;
    private Callbacks listener;

    public interface Callbacks {
        void onGameReady(Proto.GameStartedMessage message);
    }

    public RoomClient(Address hostAddress, Remote remote, Callbacks listener) throws IOException {
        this.remote = remote;
        this.remote.setListener(this);
        this.remote.start();
        this.listener = listener;

        this.hostAddress = hostAddress;

        Proto.NetworkMessage.Builder messageBuilder = Proto.NetworkMessage.newBuilder();
        messageBuilder.setType(Proto.NetworkMessage.Type.JOIN_ROOM);
        this.remote.send(this.hostAddress, messageBuilder.build());
    }

    public void Ready() throws IOException {
        this.remote.send(this.hostAddress, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.PLAYER_READY).build());
    }

    public synchronized void onReceive(Address remote, Proto.NetworkMessage message) {
        if (message.getType() == Proto.NetworkMessage.Type.GAME_STARTED) {
            /*this.remote.finish();
            try {
                this.remote.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            System.out.println("GAME STARTED");
            this.listener.onGameReady(message.getGameStartedMessage());
        }
        //System.out.println(message.getType().toString());
        //System.out.println(message.getGameStartedMessage().toString());
    }
}
