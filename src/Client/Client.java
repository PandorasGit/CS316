package Client;



import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

    private static int maxDataSize = 2048;
    private final int serverPort;
    private final InetAddress serverIP;
    private SocketChannel sc;
    private InitializedFile file;

    public Client(int serverPort, InetAddress serverIP) throws IOException {
        this.serverPort = serverPort;
        this.serverIP = serverIP;
        this.sc = connect();
    }

    public SocketChannel connect() throws IOException {
        SocketChannel sc = SocketChannel.open();
        //blocking call
        sc.connect(new InetSocketAddress(serverIP,serverPort));
        return sc;
    }


    public String service(String[] command) throws IOException {

        char c = 's';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        ByteBuffer buffer = ByteBuffer.wrap(b);

        switch (command[0]){
            case "i":
                buffer = initialize(command);
                save();
                return "File set, ready to upload";
            case "u":
                upload();
                return "Upload sent";
            case "d":

                return download();
            case "k":
                delete(command);
                return "";
            default:
                System.out.println("no valid command");
        }


        sc.write(buffer);
        sc.read(buffer);
        buffer.flip();
        System.out.println("Message from the server: " + (char)buffer.get());
        buffer.rewind();
        sc.shutdownOutput();
        sc.close();
        return "Default Return";
    }

    private void delete(String[] command) throws IOException {
        String instruction = "k" + file.name;
        byte[] b = new byte[2048];
        b = instruction.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(b);
        sc.write(buffer);
        sc.read(buffer);
        buffer.flip();
        System.out.println("Message from the server: " + (char)buffer.get());

        if ((char)buffer.get() == 'y'){
            buffer.flip();
            b = file.content.getBytes();
            buffer = ByteBuffer.wrap(b);
            sc.write(buffer);
        }

        sc.shutdownOutput();
        sc.close();
    }

    private String download() throws IOException {
        String instruction = "d";
        byte[] b = new byte[2048];
        b = instruction.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(b);
        sc.write(buffer);
        sc.read(buffer);
        sc.shutdownOutput();

        char reply = getServerCode(sc);;
        System.out.println("Can be downloaded: " + reply);
        if (reply == 'y'){
            sc.shutdownOutput();
            sc.close();
            return "Downloaded";
        }
        else {
            sc.shutdownOutput();
            sc.close();
            return "Download failed";
        }

    }

    private ByteBuffer initialize(String[] command){
        String path = "initialized./" + command[1];
        File file = new File(path);
        this.file = new InitializedFile(file.getName(), file);
        char c = 'i';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        return ByteBuffer.wrap(b);
    }

    private void initialize(String fileName){
        this.file = new InitializedFile(fileName);
    }

    private void upload() throws IOException {
        String instruction = "u" + file.name + " " + file.content;
        byte[] b = new byte[2048];
        b = instruction.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(b);
        sc.write(buffer);
        sc.read(buffer);
        buffer.flip();
        System.out.println("Message from the server: " + (char)buffer.get());
        if ((char)buffer.get() == 'y'){
            buffer.flip();
            b = file.content.getBytes();
            buffer = ByteBuffer.wrap(b);
            sc.write(buffer);
        }

        sc.shutdownOutput();
        sc.close();
    }

    private void save() throws IOException {
        final String content = file.content;
        String path = "./initialized/" + file.name;
        File file = new File(String.valueOf(path));
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
