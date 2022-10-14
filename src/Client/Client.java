package Client;



import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private int serverPort;
    private InetAddress serverIP;

    public Client(int serverPort, InetAddress serverIP) {
        this.serverPort = serverPort;
        this.serverIP = serverIP;
    }

    public void service(String command) throws IOException {


        char c = 's';

        byte[] b = new byte[1];
        b[0] = (byte) c;
        ByteBuffer buffer = ByteBuffer.wrap(b);

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
}
