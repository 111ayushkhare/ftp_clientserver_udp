import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class FTPClient {
    public static void main(String[] args) throws IOException {

        System.out.println("$Client: Client activated...");

        // Get a datagram socket
        DatagramSocket socketClient = new DatagramSocket();
        DatagramPacket sendPacketClient;

        // Get an ip address
        InetAddress ipClient = InetAddress.getLocalHost();
        byte[] bufSendClient;

        Scanner input = new Scanner(System.in);

        byte[] bufReceiveClient = new byte[1024];
        DatagramPacket receivePacketClient;

        while(true) {
            System.out.print("$Client: ");

            // Enter your message      
            String str = input.nextLine();
            bufSendClient = str.getBytes();

            // Sending request to server
            sendPacketClient = new DatagramPacket(bufSendClient,bufSendClient.length,ipClient,9999);
            socketClient.send(sendPacketClient);

            // Recieving Server's response  
            receivePacketClient = new DatagramPacket(bufReceiveClient, bufReceiveClient.length);
            socketClient.receive(receivePacketClient);

            String receivedString = new String(receivePacketClient.getData(), 0, bufReceiveClient.length);
            System.out.println("$Client: Server\'s response - " + receivedString);

            if (str.equals("stop")) {
                System.out.println("$Client: You entered \'stop\'");
                System.out.println("$Client: Client deactivated...");
                break;
            }

            // Clear the buffer after every message
            bufReceiveClient = new byte[1024];
        }
        
        // Closing the datagram socket
        socketClient.close();
        
        input.close();
    }

}
