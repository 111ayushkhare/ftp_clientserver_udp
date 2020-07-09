import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FTPServer {
    public static void main(String[] args) throws IOException {

        System.out.println("$Server: Server started...");

        // Get a datagram socket
        DatagramSocket datagramSocketServer = new DatagramSocket(9999);
        DatagramPacket datagramPacketServer;

        byte[] bufServer = new byte[1024];

        // Receiving a request
        datagramPacketServer = new DatagramPacket(bufServer,bufServer.length);
        datagramSocketServer.receive(datagramPacketServer);

        StringBuilder requestString = getRequestString(bufServer);
        System.out.println("$Server: Client has opted for choice (" + requestString + ")");

        if (requestString.toString().equals("1")) {
            System.out.println("$Server: Client has requested to know status (pass/fail)");
        } else if (requestString.toString().equals("2")) {
            System.out.println("$Server: Client has requested to get marks");
        } else if(requestString.toString().equals("0")) {
            System.out.println("$Server: Client wants to take back the request and shutdown");
        } else {
            System.out.println("$Server: Client has made INVALID REQUEST");
        }

        System.out.println("$Server: Server shuting down...");
        datagramSocketServer.close();

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
