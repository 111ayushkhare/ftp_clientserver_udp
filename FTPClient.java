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
        DatagramPacket packetClient;

        // Get an ip address
        InetAddress ip= InetAddress.getLocalHost();
        byte[] bufClient;

        Scanner input = new Scanner(System.in);

        while(true) {
            System.out.print("$Client: ");

            // Enter your message      
            String str = input.nextLine();
            bufClient = str.getBytes();

            // Sending request to server
            packetClient = new DatagramPacket(bufClient,bufClient.length,ip,9999);
            socketClient.send(packetClient);

            if (str.equals("stop")) {
                System.out.println("$Client: You entered \'stop\'");
                System.out.println("$Client: Client deactivated...");
                break;
            }
            byte[] recieveData=new byte[1024];
            DatagramPacket echoReceived=new DatagramPacket(recieveData,recieveData.length);
            socketClient.receive(echoReceived);
            String echoprint=new String(echoReceived.getData());
            System.out.println("Server : "+echoprint);
        }
        
        // Closing the datagram socket
        socketClient.close();
        
        input.close();
    }

}
