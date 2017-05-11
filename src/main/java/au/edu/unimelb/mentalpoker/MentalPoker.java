package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class MentalPoker {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello, welcome to Mental Poker!");
        System.out.println("Would you like to start or join a poker dungeon? (s/j)");

        Scanner scanner = new Scanner(System.in);
        String c = scanner.nextLine();

        int remotePort;
        int listenPort = Math.abs((new Random().nextInt() % 30000)) + 5000;

        if (c.equals("s")) {
            System.out.println("Enter the port for the new dungeon: ");
            remotePort = scanner.nextInt();
            scanner.nextLine();

            RoomHost r = new RoomHost(remotePort, new RoomHost.Callbacks() {
                @Override
                public void onConnectionFailed(String message) {
                    System.exit(1);
                }
            });
        } else {
            System.out.println("Enter the port for the existing dungeon: ");
            remotePort = scanner.nextInt();
            scanner.nextLine();
        }

        RoomClient client =
                new RoomClient(
                        remotePort,
                        listenPort,
                        new RoomClient.Callbacks() {
                            @Override
                            public void onGameReady(Proto.GameStartedMessage message, Remote remote) {
                                System.out.println("Game has started.");
                                // Create a new Poker peer client and start game.

                                PeerNetwork network = new PeerNetwork(message, remote);
                                int id = network.localId();
                                System.out.println("My id is: " + id);
                                if (id != 1) {
                                    System.out.println("Waiting for received from: " + (id - 1));
                                    Proto.NetworkMessage m = network.receive(id - 1);
                                    System.out.println("Received from: " + (id - 1));
                                }
                                if (id < network.numPlayers()) {
                                    System.out.println("Sending to: " + (id + 1));
                                    network.send(id + 1, Proto.NetworkMessage.newBuilder().setType(Proto.NetworkMessage.Type.JOIN_ROOM).build());
                                }
                            }
                        });

        System.out.println("Press any key when you are ready to play");
        scanner.nextLine();
        System.out.println("You are ready. Waiting for other players...");
        client.Ready();
    }
}
