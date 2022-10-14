package User;


import Client.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;


public class User {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: ServerTCP <port> <Server Ip>");
            return;
        }
        Client client = new Client(Integer.parseInt(args[0]), InetAddress.getByName(args[1]));
        System.out.println("File system!");

        System.out.println("Enter a command for the File System :");
        Scanner keyboard = new Scanner(System.in);
        String command = keyboard.nextLine();
        client.service("filler command");
    }

    private String parseCommand(String command){

        return "command parse";
    }
}
