package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class MentalPoker {
    static Proto.GameStartedMessage msg;

    public static void main(String[] args) throws IOException {
        System.out.println("Hello, welcome to Mental Poker!");
        System.out.println("Would you like to start or join a poker dungeon? (s/j)");

        Scanner scanner = new Scanner(System.in);
        String c = scanner.nextLine();

        String remoteIP;
        int remotePort;
        int listenPort = Math.abs((new Random().nextInt() % 30000)) + 5000;

        if (c.equals("s")) {
            remoteIP = "127.0.0.1";
            System.out.println("Enter the port for the new dungeon: ");
            remotePort = scanner.nextInt();
            scanner.nextLine();

            Remote hostRemote = new Remote(remotePort, null);
            hostRemote.setDaemon(true);
            hostRemote.start();

            RoomHost r = new RoomHost(hostRemote, new RoomHost.Callbacks() {
                @Override
                public void onConnectionFailed(String message) {
                    System.exit(1);
                }
            });
        } else {
            System.out.println("Enter the IP for the existing dungeon: ");
            remoteIP = scanner.nextLine();
            if(remoteIP.equals("")){
                remoteIP = "127.0.0.1";
            }
            System.out.println("Enter the port for the existing dungeon: ");
            remotePort = scanner.nextInt();
            scanner.nextLine();
        }

        Remote remote = new Remote(listenPort, null);
        remote.setDaemon(true);
        remote.start();

        RoomClient client = new RoomClient(new Address(remoteIP, remotePort), remote);

        while (!client.isGameStarted()) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        System.out.println("Game has started.");
        // Create a new Poker peer client and start game.

        PeerNetwork network = new PeerNetwork(client.getGameStartedMessage(), remote);
        PokerGame poker = new PokerGame(network, new SRAPokerEngine(network));
        poker.start();
    }
}
