import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTPServer {
    public static void main(String[] args) throws IOException {

        System.out.println("$Server: Server started...\n");

        // Get the port number
        int listen_port = Integer.valueOf(args[0]);

        // Get a datagram socket
        DatagramSocket socketServer = new DatagramSocket(listen_port);

        // Creating byte array to recieve and send message in bytes
        byte[] bufServer = new byte[1024];
        byte[] echoData = new byte[1024];

        while(true) {
            // Receiving a request
            System.out.print("$Server: ");
            DatagramPacket packetServer = new DatagramPacket(bufServer, bufServer.length);
            socketServer.receive(packetServer);

            String requestString = new String(packetServer.getData());
            System.out.println(requestString.trim());

            InetAddress IPAddress = packetServer.getAddress();
            int port = packetServer.getPort();
            echoData = requestString.getBytes();

            // Checking if Client wants to stop
            if (requestString.trim().equals("stop")) {
                System.out.println("$Server: Client wants to stop");
                break;
            }

            // Send the response to Client (ECHO)
            DatagramPacket echoPacket = new DatagramPacket(echoData, echoData.length, IPAddress, port);
            socketServer.send(echoPacket);

            // Clear buffer after every message;
            bufServer = new byte[1024];

            System.out.println();
        }

        // Closing the server
        System.out.println("$Server: Server shuting down...");
        socketServer.close();
    }
}
