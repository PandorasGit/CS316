package Client;



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
                return "File set, ready to upload";
            case "u":
                buffer = upload(command);
                return "Upload command sent";
            default:
                System.out.println("no valid command");
        }


        sc.write(buffer);
        sc.read(buffer);
        buffer.flip();
        System.out.println("Message from the server: " + (char)buffer.get());
        buffer.rewind();
        sc.close();
        return "Default Return";
    }

    private ByteBuffer initialize(String[] command){
        this.file = new InitializedFile(command[0], command[1]);
        char c = 'i';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        return ByteBuffer.wrap(b);
    }

    private ByteBuffer upload(String[] command){
        char c = 'u';
        byte[] b = new byte[2048];
        b[0] = (byte) c;
        return ByteBuffer.wrap(b);
    }
}
