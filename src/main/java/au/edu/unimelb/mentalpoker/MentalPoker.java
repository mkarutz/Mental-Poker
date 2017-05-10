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

        if (c.equals("s")) {
            System.out.println("Enter the port for the new dungeon: ");
            int port = scanner.nextInt();

            RoomHost r = new RoomHost(port);
        } else {
            System.out.println("Enter the port for the existing dungeon: ");
            int port = scanner.nextInt();

            RoomClient r = new RoomClient(port, (new Random().nextInt() % 30000) + 5000);
        }
    }
}
