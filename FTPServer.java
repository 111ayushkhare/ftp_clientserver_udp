import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FTPServer {
    public static void main(String[] args) throws IOException {

        System.out.println("$Server: Server started...");

        // Get a datagram socket
        DatagramSocket socketServer = new DatagramSocket(9999);
        DatagramPacket packetServer;

        byte[] bufServer = new byte[1024];

        while(true) {
            // Receiving a request
            packetServer = new DatagramPacket(bufServer,bufServer.length);
            socketServer.receive(packetServer);

            StringBuilder requestString = getRequestString(bufServer);
            System.out.println("$Server: Client's message - " + requestString);

            if (requestString.toString().equals("stop")) {
                System.out.println("$Server: Client wants to stop");
                break;
            }

            // Clear buffer after every message;
            bufServer = new byte[1024];
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
