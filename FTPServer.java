import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTPServer {
    public static void main(String[] args) throws IOException {

        System.out.println("$Server: Server started...");
int listen_port = Integer.valueOf(args[0]);

        // Get a datagram socket
        DatagramSocket socketServer = new DatagramSocket(listen_port);

        byte[] bufServer = new byte[1024];
        byte[] echoData = new byte[1024];

        while(true) {
            // Receiving a request
            DatagramPacket packetServer = new DatagramPacket(bufServer, bufServer.length);
            socketServer.receive(packetServer);

            //StringBuilder requestString = getRequestString(bufServer);
            //System.out.println("$Server: Client's message - " + requestString);
            String requestString = new String(packetServer.getData());
            System.out.println("$Server: Client's message - " + requestString);
            InetAddress IPAddres=packetServer.getAddress();
            int port =packetServer.getPort();
            echoData=requestString.getBytes();
            if (requestString.equals("stop")) {
                System.out.println("$Server: Client wants to stop");
                break;
            }
            DatagramPacket echoPacket =new DatagramPacket(echoData,echoData.length,IPAddres,port);
            socketServer.send(echoPacket);
            // Clear buffer after every message;
            bufServer = new byte[1024];
        }

        System.out.println("$Server: Server shuting down...");
        socketServer.close();

    }
}
