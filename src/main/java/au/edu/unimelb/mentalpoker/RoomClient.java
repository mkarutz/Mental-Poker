package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.util.Random;

/**
 * Created by azable on 10/05/17.
 */
public class RoomClient implements IRemoteListener {
    private Remote remote;

    public RoomClient(int hostPort, int port) throws IOException {
        this.remote = new Remote(port, this);
        this.remote.start();

        Address testRemoteAddress = new Address("127.0.0.1", hostPort);
        Proto.NetworkMessage.Builder messageBuilder = Proto.NetworkMessage.newBuilder();
        messageBuilder.setType(Proto.NetworkMessage.Type.JOIN_ROOM);
        this.remote.Send(testRemoteAddress, messageBuilder.build());
        //this.remote.Send(testRemoteAddress, new String("Hello server again").getBytes());
    }

    public synchronized void Receive(Address remote, Proto.NetworkMessage message) {
        ;
        System.out.println(message.getType().toString());
    }
}
