package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.util.Random;

/**
 * Created by azable on 10/05/17.
 */
public class RoomClient implements IRemoteListener {
    private Remote remote;
    private Address hostAddress;

    public RoomClient(int hostPort, int port) throws IOException {
        this.remote = new Remote(port, this);
        this.remote.start();

        this.hostAddress = new Address("127.0.0.1", hostPort);

        Proto.NetworkMessage.Builder messageBuilder = Proto.NetworkMessage.newBuilder();
        messageBuilder.setType(Proto.NetworkMessage.Type.JOIN_ROOM);
        this.remote.Send(this.hostAddress, messageBuilder.build());
    }

    public void Ready() throws IOException {
        this.remote.Send(this.hostAddress, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.PLAYER_READY).build());
    }

    public synchronized void Receive(Address remote, Proto.NetworkMessage message) {
        System.out.println(message.getType().toString());
    }
}
