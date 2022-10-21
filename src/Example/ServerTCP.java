package Example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerTCP {

    private static final int MAX_Client_MESSAGE_LENGTH = 1024;

    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("Usage: ServerTCP <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        ServerSocketChannel listenChannel =
                ServerSocketChannel.open();

        listenChannel.bind(new InetSocketAddress(port));

        while(true){
            //accept() is a blocking call
            //it will return only when it receives a new
            //connection request from a client
            //accept() performs the three-way handshake
            //with the client before it returns
            SocketChannel serveChannel =
                    listenChannel.accept();

            ByteBuffer buffer = ByteBuffer.allocate(MAX_Client_MESSAGE_LENGTH);
            //ensures that we read the whole message
            while(serveChannel.read((buffer)) >= 0);
            buffer.flip();
            //get the first character from the client message
            char command = (char)buffer.get();
            System.out.println("Command from client: "+ command);

            switch (command){
                case 'G':
                    //"Get": client wants to get the file
                    byte[] a = new byte[buffer.remaining()];
                    // copy the rest of the client message (i.e., the file name)
                    // to the byte array
                    buffer.get(a);
                    String fileName = new String(a);
                    File file = new File(fileName);
                    if (!file.exists() || file.isDirectory()) {
                        sendReplyCode(serveChannel, 'F');
                    }else{
                        sendReplyCode(serveChannel, 'S');
                        //read contents of file
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            //write contents of file to client
                            line = line+"\n";
                            serveChannel.write(ByteBuffer.wrap(line.getBytes()));
                        }
                    }
                    serveChannel.close();
                    break;
            }

        }
    }
    private static void sendReplyCode (SocketChannel channel, char code) throws IOException{
        byte[] a = new byte[1];
        a[0] = (byte)code;
        ByteBuffer data = ByteBuffer.wrap(a);
        channel.write(data);
    }
}