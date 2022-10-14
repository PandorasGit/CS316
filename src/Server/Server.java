package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class Server {
    public static void main(String[] args) throws IOException {
        verifyArgs(args);

        int port = Integer.parseInt(args[0]);
        ServerSocketChannel listeningSocket = defineSocketChannel(port);

        while (true) {
            // blocking call, waits until the client connects and completes 3-way-handshake
            SocketChannel serveChannel = listeningSocket.accept();

            ByteBuffer clientMessage = readClientMessage(serveChannel);
            System.out.println("Message from client: " + (char)clientMessage.get());

            // repeat message back to the client
            echoClientMessage(serveChannel, clientMessage);

            // close connection after a process is finished
            serveChannel.close();
        }
    }


    private static void verifyArgs(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ServerTCP <port>");
            System.exit(1);
        }
    }


    private static ServerSocketChannel defineSocketChannel(int port) throws IOException {
        ServerSocketChannel listeningSocket = ServerSocketChannel.open();
        listeningSocket.socket().bind(new InetSocketAddress(port));
        return listeningSocket;
    }


    private static ByteBuffer readClientMessage(SocketChannel serveChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        serveChannel.read(buffer);
        buffer.flip();
        return buffer;
    }


    private static void echoClientMessage(SocketChannel serveChannel, ByteBuffer clientMessage) throws IOException {
        clientMessage.rewind();
        // send buffer back to client
        serveChannel.write(clientMessage);
    }
}
