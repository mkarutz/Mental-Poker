package au.edu.unimelb.mentalpoker;

import java.io.IOException;
import java.util.Scanner;

public class MentalPoker {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello, welcome to Mental Poker!");

        Scanner scanner = new Scanner(System.in);
        String c = scanner.nextLine();
        System.out.println(c);

        if (c.equals("y")) {
            RoomHost r = new RoomHost();
        } else {
            RoomClient r = new RoomClient();
        }
    }
}
