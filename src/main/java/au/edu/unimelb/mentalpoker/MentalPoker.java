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
                        Math.abs((new Random().nextInt() % 30000)) + 5000,
                        new RoomClient.Callbacks() {
                            @Override
                            public void onGameReady(Proto.GameStartedMessage message) {
                                // Create a new Poker peer client and start game.
                            }
                        });

        System.out.println("Press any key when you are ready to play");
        scanner.nextLine();
        client.Ready();
    }
}
