package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by azable on 10/05/17.
 */
public class RoomClient implements Remote.Callbacks {
    private Remote remote;
    private Address hostAddress;
    private boolean joinAllowed;
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
        System.out.println("Attempting to join the specified poker dungeon...");
    }

    public void ready() {
        try {
            this.remote.send(this.hostAddress, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.PLAYER_READY).build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onReceive(Address remote, Proto.NetworkMessage message) {
        if (message.getType() == Proto.NetworkMessage.Type.JOIN_ROOM_ALLOWED) {
            System.out.println("...you have been accepted into the poker dungeon.");

            System.out.println("Press any key when you are ready to play");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            ready();
            System.out.println("You are ready. Waiting for other players...");

        } if (message.getType() == Proto.NetworkMessage.Type.GAME_STARTED) {
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
