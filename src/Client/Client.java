package Client;



import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

    private final int serverPort;
    private final InetAddress serverIP;

    public Client(int serverPort, InetAddress serverIP) {
        this.serverPort = serverPort;
        this.serverIP = serverIP;
    }

    public void service(String command) throws IOException {
        char c = 's';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        ByteBuffer buffer = ByteBuffer.wrap(b);

        if (command.equals("i")) {
            buffer = initialize();
        }
        else if (command.equals("u")) {
            buffer = upload();
        }



        SocketChannel sc = SocketChannel.open();
        //blocking call
        sc.connect(new InetSocketAddress(serverIP,serverPort));

        sc.write(buffer);
        sc.read(buffer);
        buffer.flip();
        System.out.println("Message from the server: " + (char)buffer.get());
        buffer.rewind();
        sc.close();
    }

    private ByteBuffer initialize(){
        char c = 'i';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        return ByteBuffer.wrap(b);
    }

    private ByteBuffer upload(){
        char c = 'u';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        return ByteBuffer.wrap(b);
    }
}
