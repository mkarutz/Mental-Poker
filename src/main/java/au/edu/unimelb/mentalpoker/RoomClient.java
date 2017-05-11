package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 10/05/17.
 */
public class RoomClient implements Remote.Callbacks {
    private Remote remote;
    private Address hostAddress;

    public RoomClient(int hostPort, int port) throws IOException {
        this.remote = new Remote(port, this);
        this.remote.start();

        this.hostAddress = new Address("127.0.0.1", hostPort);

        Proto.NetworkMessage.Builder messageBuilder = Proto.NetworkMessage.newBuilder();
        messageBuilder.setType(Proto.NetworkMessage.Type.JOIN_ROOM);
        this.remote.send(this.hostAddress, messageBuilder.build());
    }

    public void Ready() throws IOException {
        this.remote.send(this.hostAddress, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.PLAYER_READY).build());
    }

    public synchronized void onReceive(Address remote, Proto.NetworkMessage message) {
        System.out.println(message.getType().toString());
        System.out.println(message.getGameStartedMessage().toString());
    }
}
