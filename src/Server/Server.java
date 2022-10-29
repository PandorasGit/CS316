package Server;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Server {

    private static final int MAX_CLIENT_MESSAGE_LENGTH = 2048;

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
            byte[] fileNameAsBytes;
            String fileName;
            File file;
            switch (clientCommand) {
                case 'u':
                    byte[] fileAsBytes = new byte[clientMessage.remaining()];
                    clientMessage.get(fileAsBytes);
                    String fileString = new String(fileAsBytes);
                    String[] fileStringArray = fileString.split(" ", 2);
                    file = new File("./uploaded/" + fileStringArray[0]);

                    if(!file.exists()) {
                        sendReplyCode(serveChannel, 'y');
                        System.out.println("file created");
                        Files.createDirectories(Paths.get("./uploaded"));
                        //make sure to set the "append" flag to true
                        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
                        bw.write(fileStringArray[1]);
                        bw.flush();
                        bw.close();
                        System.out.println(fileStringArray[1]);
                        addFileToListCSV(fileStringArray);

                    }else{
                        sendReplyCode(serveChannel, 'n');
                        System.out.println("\nUpload failed, file exists.");
                    }
                    break;

                case 'd':
                    fileNameAsBytes = new byte[clientMessage.remaining()];
                    clientMessage.get(fileNameAsBytes);
                    fileName = new String(fileNameAsBytes);
                    file = new File("./uploaded/"+ fileName);

                    if(!file.exists() || file.isDirectory()) {
                        System.out.println("File does not exist, or does exist and is a directory.");
                        sendReplyCode(serveChannel, 'n');
                    }else{
                        sendReplyCode(serveChannel, 'y');
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
                        serveChannel.shutdownOutput();
                    }
                    break;

                case 'k':
                    fileNameAsBytes = new byte[clientMessage.remaining()];
                    clientMessage.get(fileNameAsBytes);
                    fileName = new String(fileNameAsBytes);
                    file = new File("./uploaded/" + fileName);

                    if(file.exists()) {
                        //Tries deleting the file and checks if it was successful
                        if(file.delete()) {
                            System.out.println("File deleted.");
                        }
                        sendReplyCode(serveChannel, 'y');
                    }else{
                        System.out.println("File does not exist");
                        sendReplyCode(serveChannel, 'n');
                    }
                    break;

                case 'r':
                    byte[] fileNamesAsBytes = new byte[clientMessage.remaining()];
                    clientMessage.get(fileNamesAsBytes);
                    String fileNamesAsString = new String(fileNamesAsBytes);
                    String[] fileNameArray = fileNamesAsString.split(" ", 2);

                    //Create file paths for the rename operation
                    Path oldFilePath = Path.of("./uploaded/" + fileNameArray[0]);
                    Path renamedFilePath = Path.of("./uploaded/" + fileNameArray[1]);

                    //Check if the file exists, and then renames it
                    if(oldFilePath.toFile().exists()) {
                        Files.move(oldFilePath, renamedFilePath);
                        System.out.println("Rename successful");
                        sendReplyCode(serveChannel, 'y');
                    }else{
                        System.out.println("File does not exist");
                        sendReplyCode(serveChannel, 'n');
                    }
                    break;

                case 'l':
                    BufferedReader csvReader = new BufferedReader(new FileReader("./uploaded/fileCsvList.csv"));
                    String fileListAsString = "";
                    String currentRow;

                    while ((currentRow = csvReader.readLine()) != null) {
                        String[] data = currentRow.split(",");
                        fileListAsString = fileListAsString.concat(data[0] + ", ");
                    }
                    csvReader.close();

                    byte[] fileListAsBytes = fileListAsString.getBytes();
                    ByteBuffer buffer = ByteBuffer.wrap(fileListAsBytes);
                    serveChannel.write(buffer);
                    serveChannel.shutdownOutput();
                    break;

                default:
                    System.out.println("Not a valid command");
                    break;
            }
            serveChannel.close();
        }
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


    private static void addFileToListCSV(String[] fileArray) throws IOException {
        String fileName = fileArray[0];
        try (FileWriter writer = new FileWriter("./uploaded/fileCsvList.csv", true)) {
            writer.append(fileName).append(",\n");
            writer.flush();
        }
    }


//    private static void downloadFile(SocketChannel serveChannel, File file) throws IOException {
//        System.out.println("file sent to client");
//        //send file to client assumed as text
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        String line;
//        //loops until entire file has been read and sent to the client
//        while((line = reader.readLine()) != null) {
//            line = line + "\n";
//            //send bytes to client
//            serveChannel.write(ByteBuffer.wrap(line.getBytes()));
//        }
//        serveChannel.shutdownOutput();
//    }


//    private static void deleteFile(File file) {
//        //Tries deleting the file and checks if it was successful
//        if(file.delete()) {
//            System.out.println("File deleted.");
//        }
//    }


//    private static void renameFile(Path filePath) throws IOException {
//        Files.move(filePath, Path.of(filePath.toString() + "_renamed"));
//    }


    private static void listFiles(SocketChannel serveChannel) throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader("./uploaded/fileCsvList.csv"));
        String fileListAsString = "";
        String currentRow;

        while ((currentRow = csvReader.readLine()) != null) {
            String[] data = currentRow.split(",");
            fileListAsString = fileListAsString.concat(data[0] + ", ");
        }
        csvReader.close();

        byte[] fileListAsBytes = fileListAsString.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(fileListAsBytes);
        serveChannel.write(buffer);
        serveChannel.shutdownOutput();
    }


    private static void sendReplyCode(SocketChannel serveChannel, char code) throws IOException {
        byte[] a = new byte[1];
        a[0] = (byte)code;
        ByteBuffer data = ByteBuffer.wrap(a);
        serveChannel.write(data);
    }
}
