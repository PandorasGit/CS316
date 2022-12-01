package User;


import Client.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Scanner;


public class User {


    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: ServerTCP <port> <Server Ip>");
            return;
        }

        User user = new User();

        Client client = new Client(Integer.parseInt(args[0]), InetAddress.getByName(args[1]));
        System.out.println("File system!");
        while(true) {
            System.out.println("Enter a command for the File System :");
            Scanner keyboard = new Scanner(System.in);
            String command = keyboard.nextLine();
            String[] command_array = user.parseCommand(command);
            if (!command_array[0].equals("i")) {
                client.connect();
            }
            System.out.println(client.service(command_array));
            if (command_array[0].equals("q")) {
                break;
            }
        }

    }

    private String[] parseCommand(String command) {
        //Split the command string into an array based on the escape character
        String regex = "-";
        String[] command_split_array = command.split(regex, 2);
        //Remove any spaces from the first section of the command
        command = command_split_array[0].strip();
        command = command.toLowerCase(Locale.ROOT);


        command_split_array[0] = firstCommand(command);
        return command_split_array;
    }

    private String firstCommand(String command){
            return switch (command) {
                case "initialize" -> "i";
                case "upload" -> "u";
                case "download" -> "d";
                case "delete" -> "k";
                case "rename" -> "r";
                case "list" -> "l";
                case "quit" -> "q";
                default -> "n";
            };
    }


}
