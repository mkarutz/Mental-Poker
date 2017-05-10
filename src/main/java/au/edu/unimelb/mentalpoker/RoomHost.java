package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 10/05/17.
 */
public class RoomHost implements IRemoteListener {

    private Remote remote;

    public RoomHost() {
        this.remote = new Remote(4001, this);
        this.remote.start();
    }

    public synchronized void Receive(Address remote, byte[] message) {
        System.out.println(new String(message));
        try {
            this.remote.Send(remote, message);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
