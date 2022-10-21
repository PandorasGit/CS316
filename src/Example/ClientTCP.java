package Example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


public class ClientTCP {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java Client <server_IP> <server_port>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        String serverIP = args[0];

        char command;

        do{
            Scanner keyboard = new Scanner(System.in);
            System.out.println("Enter a command (D, G, L, R, U, or Q):");
            //Commands are NOT case-sensitive.
            command = keyboard.nextLine().toUpperCase().charAt(0);

            switch (command) {
                case 'G':
                    System.out.println("Enter the name of the file to download: ");
                    String fileName = keyboard.nextLine();
                    ByteBuffer buffer = ByteBuffer.wrap(("G" + fileName).getBytes());
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverIP, serverPort));
                    channel.write(buffer);
                    //It's critical to shut down output on client side
                    //when client is done sending to server
                    channel.shutdownOutput();
                    //receive server reply code
                    if (getServerCode(channel) != 'F') {
                        System.out.println("Server failed to serve the request.");
                    } else {
                        System.out.println("The request was accepted");
                        Files.createDirectories(Paths.get("./downloaded"));
                        //make sure to set the "append" flag to true
                        BufferedWriter bw = new BufferedWriter(new FileWriter("./downloaded/"+fileName, true));
                        ByteBuffer data = ByteBuffer.allocate(1024);
                        int bytesRead;

                        while ((bytesRead = channel.read(data)) != -1) {
                            //before reading from buffer, flip buffer
                            //("limit" set to current position, "position" set to zero)
                            data.flip();
                            byte[] a = new byte[bytesRead];
                            //copy bytes from buffer to array
                            //(all bytes between "position" and "limit" are copied)
                            data.get(a);
                            String serverMessage = new String(a);
                            bw.write(serverMessage);
                            data.clear();
                        }
                        bw.close();
                    }
                    channel.close();
                    break;
            }
        }while(command != 'Q');

    }

    private static char getServerCode(SocketChannel channel) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int bytesToRead = 1;

        //make sure we read the entire server reply
        while((bytesToRead -= channel.read(buffer)) > 0);

        //before reading from buffer, flip buffer
        buffer.flip();
        byte[] a = new byte[1];
        //copy bytes from buffer to array
        buffer.get(a);
        char serverReplyCode = new String(a).charAt(0);

        System.out.println(serverReplyCode);

        return serverReplyCode;
    }
}
