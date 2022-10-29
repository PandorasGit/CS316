package Client;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {


    private final int serverPort;
    private final InetAddress serverIP;
    private SocketChannel sc;
    private InitializedFile file;

    public Client(int serverPort, InetAddress serverIP) {
        this.serverPort = serverPort;
        this.serverIP = serverIP;
    }

    public void connect() throws IOException {
        this.sc = SocketChannel.open();
        //blocking call
        sc.connect(new InetSocketAddress(serverIP,serverPort));
    }


    public String service(String[] command) throws IOException {
        char c = 's';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        ByteBuffer buffer = ByteBuffer.wrap(b);

        switch (command[0]) {
            case "i":
                initialize(command);
                saveInitialized();
                return "File set, ready to upload";
            case "u":
                String instruction = "u" + file.name + " " + file.content;
                b = new byte[2048];
                b = instruction.getBytes();
                buffer = ByteBuffer.wrap(b);
                sc.write(buffer);
                sc.shutdownOutput();
                buffer.clear();
                sc.read(buffer);
                buffer.flip();
                char serverMessage = (char) buffer.get();
                System.out.println("Message from the server: " + serverMessage);
                sc.close();
                if (serverMessage == 'y') {
                    return "Upload successful";
                } else {
                    return "Upload failed";
                }
            case "d":
                instruction = "d" + file.name;
                b = new byte[2048];
                b = instruction.getBytes();
                buffer = ByteBuffer.wrap(b);
                sc.write(buffer);
                sc.read(buffer);
                sc.shutdownOutput();

                char reply = getServerCode(sc);
                System.out.println("Can be downloaded: " + reply);
                if (reply == 'y') {
                    System.out.println("The request was accepted");
                    Files.createDirectories(Paths.get("./downloaded"));
                    //make sure to set the "append" flag to true
                    BufferedWriter bw = new BufferedWriter(new FileWriter("./downloaded/" + file.name, true));
                    ByteBuffer data = ByteBuffer.allocate(1024);
                    int bytesRead;

                    while ((bytesRead = sc.read(data)) != -1) {
                        //before reading from buffer, flip buffer
                        //("limit" set to current position, "position" set to zero)
                        data.flip();
                        byte[] a = new byte[bytesRead];
                        //copy bytes from buffer to array
                        //(all bytes between "position" and "limit" are copied)
                        data.get(a);
                        String fileString = new String(a);
                        bw.write(fileString);
                        data.clear();
                    }
                    bw.close();

                    return "Download Successful";
                } else {
                    sc.shutdownOutput();
                    sc.close();
                    return "Download failed";
                }
            case "k":
                instruction = "k" + file.name;
                b = new byte[2048];
                b = instruction.getBytes();
                buffer = ByteBuffer.wrap(b);
                sc.write(buffer);
                sc.shutdownOutput();
                buffer.clear();
                sc.read(buffer);
                buffer.flip();
                serverMessage = (char) buffer.get();
                System.out.println("Message from the server: " + serverMessage);
                sc.close();
                if (serverMessage == 'y') {
                    return "Delete Successful";
                } else {
                    return "Delete Failed";
                }
            case "r":
                try {
                    instruction = "r" + file.name + " " + command[1];
                    b = new byte[2048];
                    b = instruction.getBytes();
                    buffer = ByteBuffer.wrap(b);
                    sc.write(buffer);
                    sc.shutdownOutput();
                    buffer.clear();
                    sc.read(buffer);
                    buffer.flip();
                    serverMessage = (char) buffer.get();
                    System.out.println("Message from the server: " + serverMessage);
                    sc.close();
                    if (serverMessage == 'y') {
                        return "Rename Successful";
                    } else {
                        return "Rename Failed";
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    sc.close();
                    return "Rename syntax error. Initialize the file you want to rename then use\nrename -<New Name>";
                }
            case "l":
                byte[] a = new byte[1];
                a[0] = 'l';
                buffer = ByteBuffer.wrap(a);
                sc.write(buffer);
                sc.shutdownOutput();
                buffer.clear();
                ByteBuffer serverReply = ByteBuffer.allocate(1024);
                int bytesRead;
                String list ="";
                while ((bytesRead = sc.read(serverReply)) != -1) {
                    //before reading from buffer, flip buffer
                    //("limit" set to current position, "position" set to zero)
                    serverReply.flip();
                    byte[] listBytes = new byte[bytesRead];
                    //copy bytes from buffer to array
                    //(all bytes between "position" and "limit" are copied)
                    serverReply.get(listBytes);
                    String fileString = new String(listBytes);
                    list += list + fileString;
                    serverReply.clear();
                }
                return "Files in the server:" + list;

            default:
                a = new byte[1];
                a[0] = 's';
                ByteBuffer data = ByteBuffer.wrap(a);
                sc.write(data);
                sc.close();
                return "no valid command";
        }
    }



    private void initialize(String[] command){
        String path = "initialized./" + command[1];
        File file = new File(path);
        this.file = new InitializedFile(file.getName(), file);
    }


    private void saveInitialized() throws IOException {
        String path = "./initialized/" + file.name;
        File file = new File(path);
        file.createNewFile();
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
