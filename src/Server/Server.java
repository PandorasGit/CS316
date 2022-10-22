package Server;

import Client.InitializedFile;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Server {

    private static final int MAX_CLIENT_MESSAGE_LENGTH = 2048;
    private static InitializedFile initializedFile;

    public static void main(String[] args) throws IOException {
        verifyArgs(args);
        int port = Integer.parseInt(args[0]);

        ServerSocketChannel listeningSocket = ServerSocketChannel.open();
        listeningSocket.bind(new InetSocketAddress(port));

        while (true) {
            // blocking call, waits until the client connects and completes 3-way-handshake
            SocketChannel serveChannel = listeningSocket.accept();
            ByteBuffer clientMessage = readClientMessage(serveChannel);

            char clientCommand = (char)clientMessage.get();
            System.out.println("Message from client: " + clientCommand);
            String fileName;
            File file;
            switch (clientCommand) {
                case 'u':
                    byte[] fileNameAsBytes = new byte[clientMessage.remaining()];
                    clientMessage.get(fileNameAsBytes);
                    fileName = new String(fileNameAsBytes);
                    file = new File("./uploaded/" + fileName);

                    if(!file.exists()) {
                        sendReplyCode(serveChannel, 'y');
                        uploadFile(file);
                    }else{
                        sendReplyCode(serveChannel, 'n');
                        System.out.println("\nUpload failed, file exists.");
                    }
                    serveChannel.close();
                    break;
                //when the client wants to download the file
                case 'd':
                    //read the rest of the buffer
                    byte[] a = new byte[clientMessage.remaining()];
                    clientMessage.get(a);
                    fileName = new String(a);
                    //check if file exists
                    file = new File(fileName);
                    if(!file.exists() || file.isDirectory()) {
                        System.out.println("File does not exist, or does exist and is a directory.");
                        sendReplyCode(serveChannel, 'n');
                    }else{
                        sendReplyCode(serveChannel, 'y');
                        downloadFile(serveChannel, file);
                    }
                    serveChannel.close();
                    break;
                case 'k':
                    deleteFile();
                    sendReplyCode(serveChannel, 'k');
                    serveChannel.close();
                    break;
                case 'r':
                    renameFile();
                    sendReplyCode(serveChannel, 'r');
                    serveChannel.close();
                    break;
                case 'l':
                    listFiles();
                    sendReplyCode(serveChannel, 'l');
                    serveChannel.close();
                    break;
                case 's':
                    System.out.println("client sent s");
                    sendReplyCode(serveChannel, 's');
                    serveChannel.close();
                    break;
            }

            // repeat message back to the client
            //echoClientMessage(serveChannel, clientMessage);
        }
    }


    private static void listFiles() {
        System.out.println("list: ");
    }


    private static void renameFile() {
        System.out.println("rename");
    }


    private static void deleteFile() {
        System.out.println("delete");
    }


    private static void uploadFile(File file) throws IOException {
        System.out.println("file created");
        Files.createDirectories(Paths.get("./uploaded"));
        //make sure to set the "append" flag to true
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
    }


    private static void downloadFile(SocketChannel serveChannel, File file) throws IOException {
        System.out.println("file sent to client");
        //send file to client assumed as text
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        //loops until entire file has been read and sent to the client
        while((line = reader.readLine()) != null) {
            line = line + "\n";
            //send bytes to client
            serveChannel.write(ByteBuffer.wrap(line.getBytes()));
        }
    }


    private static void sendReplyCode(SocketChannel serveChannel, char code) throws IOException {
        byte[] a = new byte[1];
        a[0] = (byte)code;
        ByteBuffer data = ByteBuffer.wrap(a);
        serveChannel.write(data);
    }


    private static void verifyArgs(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ServerTCP <port>");
            System.exit(1);
        }
    }


    private static ByteBuffer readClientMessage(SocketChannel serveChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_CLIENT_MESSAGE_LENGTH);
        while(serveChannel.read(buffer) >= 0);
        buffer.flip();

        return buffer;
    }
}
