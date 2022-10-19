package Client;



import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
        if (command.length != 2) {
            return "Use the following syntax <command> -<parameter>";
        }
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
                buffer = upload(command);
                return "Upload command sent";
            case "d":
                buffer = download(command);
                return "Download comment sent";
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

    private ByteBuffer download(String[] command) {
        char c = 'd';
        byte[] b = new byte[1];
        b[0] = (byte) c;
        return ByteBuffer.wrap(b);

    }

    private ByteBuffer initialize(String[] command){
        File file = new File(command[1]);
        this.file = new InitializedFile(file.getName(), file);
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

    private void save() throws IOException {
        final String content = this.file.content;
        final Path path = Paths.get(String.format("%s", this.file.name));

        try (
                final BufferedWriter writer = Files.newBufferedWriter(path,
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        ) {
            writer.write(content);
            writer.flush();
        }
    }
}
