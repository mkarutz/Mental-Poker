package au.edu.unimelb.mentalpoker.net;

import au.edu.unimelb.mentalpoker.Proto;

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
        this.remote.setTimeOutInterval(5000);
        this.gameStarted = false;

        this.hostAddress = hostAddress;

        Proto.NetworkMessage.Builder messageBuilder = Proto.NetworkMessage.newBuilder();
        messageBuilder.setType(Proto.NetworkMessage.Type.JOIN_ROOM);
        this.remote.send(this.hostAddress, messageBuilder.build());
        System.out.println("Attempting to join the specified poker dungeon...");
    }

    public void ready() {
        this.remote.send(this.hostAddress, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.PLAYER_READY).build());
    }

    public void onReceive(Address source, Proto.NetworkMessage message) {
        if (message.getType() == Proto.NetworkMessage.Type.JOIN_ROOM_ALLOWED) {
            System.out.println("...you have been accepted into the poker dungeon.");

            System.out.println("Press any key when you are ready to play");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            ready();
            System.out.println("You are ready. Waiting for other players...");

        } else if (message.getType() == Proto.NetworkMessage.Type.GAME_STARTED) {
            this.gameStarted = true;
            this.gameStartedMessage = message.getGameStartedMessage();

        } else if (message.getType() == Proto.NetworkMessage.Type.REQUEST_IP) {
            Proto.NetworkMessage reply = Proto.NetworkMessage.newBuilder()
                    .setType(Proto.NetworkMessage.Type.REQUEST_IP_RESULT)
                    .setRequestIpResultMessage(
                            Proto.RequestIpResultMessage.newBuilder()
                                    .setIp(source.getIp()))
                    .build();
            this.remote.send(source, reply);
        }
    }

    @Override
    public void onSendFailed(Address destination) {
        System.out.println("Error: Could not send message to: " + destination);
    }

    public boolean isGameStarted() {
        return this.gameStarted;
    }

    public Proto.GameStartedMessage getGameStartedMessage() {
        return this.gameStartedMessage;
    }
}
