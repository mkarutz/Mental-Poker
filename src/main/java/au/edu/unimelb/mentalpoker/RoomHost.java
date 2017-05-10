package au.edu.unimelb.mentalpoker;

import java.io.IOException;

/**
 * Created by azable on 10/05/17.
 */
public class RoomHost implements IRemoteListener {

    private Remote remote;
    private GameTable gameTable;

    public RoomHost(int port) {
        this.gameTable = new GameTable();
        this.remote = new Remote(port, this);
        this.remote.start();
    }

    public void Receive(Address remote, Proto.NetworkMessage message) {
        //System.out.println(new String(message));
        try {
            this.remote.Send(remote, message);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
