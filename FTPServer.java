import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTPServer {
    public static void main(String[] args) throws IOException {

        System.out.println("$Server: Server started...");

        // Get a datagram socket
        DatagramSocket socketServer = new DatagramSocket(9999);
        DatagramPacket receivePacketServer;

        // Server IP address
        InetAddress ipServer = InetAddress.getLocalHost();

        // Recieve byte array
        byte[] bufReceiveServer = new byte[1024];

        byte[] bufSendServer;
        DatagramPacket sendPacketServer;

        while(true) {
            // Receiving a request
            receivePacketServer = new DatagramPacket(bufReceiveServer, bufReceiveServer.length);
            socketServer.receive(receivePacketServer);

            StringBuilder requestString = getRequestString(bufReceiveServer);
            System.out.println("$Server: Client's message - " + requestString);

            // Send byte array
            String responseString = requestString.toString();
            bufSendServer = responseString.getBytes();

            // Response by sending message back to Server
            sendPacketServer = new DatagramPacket(bufSendServer,bufSendServer.length,ipServer,9999);
            socketServer.send(sendPacketServer); 
            
            if (requestString.toString().equals("stop")) {
                System.out.println("$Server: Client wants to quit");
                break;
            }

            // Clear buffer after every message
            bufReceiveServer = new byte[1024];

        }

        System.out.println("$Server: Server shuting down...");
        socketServer.close();

    }

    public static StringBuilder getRequestString(byte[] str) {
        if (str == null) {
            return null;
        }
        StringBuilder reqStr = new StringBuilder();
        int i = 0;
        while (str[i] != 0) {
            reqStr.append((char) str[i]);
            i++;
        }
        return reqStr;
    }
}
