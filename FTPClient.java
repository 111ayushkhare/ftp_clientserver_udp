import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class FTPClient {
    public static void main(String[] args) throws IOException {

        System.out.println("$Client: Client activated...");

        // Get a datagram socket
        DatagramSocket datagramSocketClient = new DatagramSocket();
        DatagramPacket datagramPacketClient;

        // Get ip address
        InetAddress ip= InetAddress.getLocalHost();

        System.out.println("$Client: Press (1) to get your status passed/failed");
        System.out.println("$Client: Press (2) to get your marks");
        System.out.println("$Client: Press (0) to shutdown");
        System.out.println("$Client: Client requesting...");
        System.out.print("$Client: ");

        // Enter your choice
        Scanner input = new Scanner(System.in);
        String choice = input.next();
        byte[] bufClient = choice.getBytes();

        // Sending request to server
        datagramPacketClient = new DatagramPacket(bufClient,bufClient.length,ip,9999);
        datagramSocketClient.send(datagramPacketClient);

        // Closing the datagram socket
        datagramSocketClient.close();
    }

}
