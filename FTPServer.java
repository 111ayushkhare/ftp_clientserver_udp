import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.io.FileInputStream;

public class FTPServer {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Syntax: FTPServer <port>");
            return;
        }

        System.out.println("$Server: Server started...\n");

        // Get the port number
        int listen_port = Integer.valueOf(args[0]);

        // Get a datagram socket
        DatagramSocket socketServer = new DatagramSocket(listen_port);

        // Creating byte array to recieve and send message in bytes
        byte[] bufServer = new byte[1024];
        byte[] sendData = new byte[1024];

        while(true) {
            // Receiving a request
            System.out.print("$Server: ");
            DatagramPacket packetServer = new DatagramPacket(bufServer, bufServer.length);
            socketServer.receive(packetServer);

            String requestString = new String(packetServer.getData());
            System.out.println(requestString.trim());

            StringBuilder sb = new StringBuilder("\n");

            if(requestString.trim().equals("file")) {

                // Reading text file 
                FileInputStream fileIn = new FileInputStream("./hello.txt");
                int i;
                do{
                    i = fileIn.read();
                    if (i != -1) {
                        sb.append((char)i);
                    }
                } while (i != -1);
                fileIn.close();

            } else {
                // If Client types command other than 'file'
                sb.append("Unknown command, correct command - \'file\'\n$Client: Waiting for next update...");
            }

                
            InetAddress IPAddress = packetServer.getAddress();
            int port = packetServer.getPort();

            // Converting data to bytes
            sendData = (sb.toString()).getBytes();

            // Checking if Client wants to stop
            if (requestString.trim().equals("stop")) {
                System.out.println("$Server: Client wants to stop");
                break;
            }

            // Send the response to Client (ECHO)
            DatagramPacket echoPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            socketServer.send(echoPacket);
            System.out.println("$Server: Response sent.");
            
            // Clear buffer after every message;
            bufServer = new byte[1024];

            System.out.println();
            }

        // Closing the server
        System.out.println("$Server: Server shuting down...");
        socketServer.close();
    }
}
