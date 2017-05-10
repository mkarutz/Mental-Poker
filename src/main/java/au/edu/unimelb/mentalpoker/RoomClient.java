package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.util.Random;

/**
 * Created by azable on 10/05/17.
 */
public class RoomClient implements IRemoteListener {
    private Remote remote;

    public RoomClient() throws IOException {
        this.remote = new Remote((new Random().nextInt() % 30000) + 5000, this);
        this.remote.start();

        Address testRemoteAddress = new Address("127.0.0.1", 4001);
        this.remote.Send(testRemoteAddress, new String("Hello server").getBytes());
    }

    public synchronized void Receive(Address remote, byte[] message) {
        System.out.println(new String(message));
    }
}
