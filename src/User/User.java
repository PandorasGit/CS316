package User;


import Client.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Scanner;


public class User {

    // may use this to store the other half of the commands? unsure yet
    private String initializedFile;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: ServerTCP <port> <Server Ip>");
            return;
        }

        User user = new User();

        Client client = new Client(Integer.parseInt(args[0]), InetAddress.getByName(args[1]));
        System.out.println("File system!");

        System.out.println("Enter a command for the File System :");
        Scanner keyboard = new Scanner(System.in);
        String command = keyboard.nextLine();
        command = user.parseCommand(command);
        client.service(command);
    }

    private String parseCommand(String command){
        //Split the command string into an array based on the escape character
        String regex = "-";
        String[] command_split_array = command.split(regex);
        //Remove any spaces from the first section of the command
        command = command_split_array[0].strip();
        command = command.toLowerCase(Locale.ROOT);

        return switch (command) {
            case "initialize" -> "i";
            case "upload" -> "u";
            case "download" -> "d";
            case "delete" -> "k";
            default -> "n";
        };

    }
}
