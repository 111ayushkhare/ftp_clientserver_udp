import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.io.FileOutputStream;

public class FTPClient {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Syntax: FTPClient <hostName or IPV4 address> <port>");
            return;
        }
        System.out.println("$Client: Client activated...\n");

        // Get a datagram socket
        DatagramSocket socketClient = new DatagramSocket();

        // Get a datagram packet
        DatagramPacket packetClient;

        // Get an ip address of localhost or the server
        InetAddress ip = InetAddress.getByName(args[0]);

        // Get port number
        int port = Integer.valueOf(args[1]);

        byte[] bufClient;

        Scanner input = new Scanner(System.in);

        while(true) {
            System.out.print("$Client: ");

            // Enter your message
            String str = input.nextLine();
            // Converting message to bytes
            bufClient = str.getBytes();

            // Sending request to server
            packetClient = new DatagramPacket(bufClient, bufClient.length, ip, port);
            socketClient.send(packetClient);

            // Checking if client wants to stop
            if (str.equals("stop")) {
                System.out.println("$Client: You entered \'stop\'");
                System.out.println("$Client: Client deactivated...");
                break;
            }

            // Recieving response (ECHO) from server
            byte[] recieveData = new byte[1024];
            DatagramPacket echoRecieved = new DatagramPacket(recieveData, recieveData.length);
            socketClient.receive(echoRecieved);

            // Converting recieved byte data into String
            String echoPrint = new String(echoRecieved.getData()).trim();
            System.out.println("$Client: Server\'s response - " + echoPrint);

            // Creating a new file and writing the recieved text file data to it
            FileOutputStream fileOut = new FileOutputStream("./abc.txt", true);
            char[] ch = echoPrint.toCharArray();
            for (int i = 0; i < ch.length; i++) {
                fileOut.write(ch[i]);
            }
            fileOut.close();


            System.out.println();
        }

        // Closing the datagram socket
        socketClient.close();

        input.close();
    }

}
