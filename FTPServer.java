import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class FTPServer {
    static int packet = 0;
    static int expectedAck = 0;
    static final int timeout = 1000;
    static int packetLoss = 0;
    static{
        System.out.println("Default Port number in use is 8954");
        System.out.println("If you want to change it pass it as command line argument");
        System.out.println("As: java FTPServer <PortNo>");
    }
    public static void main(String[] args) throws IOException {


        System.out.println("$Server: Server started...\n");

        // Get the port number
        int listen_port = 8954;
        if(args.length != 0) {
            listen_port = Integer.valueOf(args[0]);
        }
        int bufsize = 1024;

        // Get a datagram socket
        DatagramSocket socketServer = new DatagramSocket(listen_port);


        // Creating byte array to recieve and send message in bytes
        byte[] bufServer = new byte[bufsize];
        byte[] sendData = new byte[bufsize];

        while(true) {
            // Receiving a request
            System.out.print("$Server: ");
            DatagramPacket packetServer = new DatagramPacket(bufServer, bufServer.length);
            socketServer.receive(packetServer);

            InetAddress IPAddress = packetServer.getAddress();
            int port = packetServer.getPort();

            String requestString = new String(packetServer.getData());
            System.out.println(requestString.trim());

            StringBuilder sb = new StringBuilder("\n");
            if (requestString.trim().equals("ls"))
            {
                String currentDirectory = System.getProperty("user.dir");
                File directoryPath=new File(currentDirectory);
                File fileList[] = directoryPath.listFiles();
                for (File file: fileList)
                    sb.append(file+"\n");

            }
            else if(requestString.trim().startsWith("cd "))
            {
                String command = requestString.trim().substring(3)+"/";
                File directoryPath=new File(command);
                File fileList[] = directoryPath.listFiles();
                for (File file: fileList)
                    sb.append(file+"\n");
            }

            else if(requestString.trim().startsWith("file")) {
                String TobeSent = requestString.trim().substring(5);
                StringBuilder filesb = new StringBuilder("\n");
                filesb.append("sending ");
                filesb.append(TobeSent);
                sendData = (filesb.toString()).getBytes();
                DatagramPacket echoPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                socketServer.send(echoPacket);

                FileInputStream fileIn = new FileInputStream("./"+TobeSent);
                byte[] buffer = new byte[508];
                byte[] sizebyte = Helper.int2ByteArray(512);
                int size=0;
                DatagramPacket fileContent = new DatagramPacket(new byte[0], 0, IPAddress, port);
                while ((size=fileIn.read(buffer)) != -1) {
                    sizebyte=Helper.int2ByteArray(size);
                    buffer=Arrays.copyOf(buffer,512);
                    buffer[508] = sizebyte[0];
                    buffer[509] = sizebyte[1];
                    buffer[510] = sizebyte[2];
                    buffer[511] = sizebyte[3];
                    sendDataToClient(buffer, IPAddress, port, timeout);
                    sendData = new byte[508];
                }
                fileIn.close();
                continue;

            } else {
                // If Client types command other than 'file'
                sb.append("Unknown command, correct command - \'file\'\n$Client: Waiting for next update...");
            }




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
    public static void sendDataToClient(byte[] sendData, InetAddress IPAddress, int port, int timeout) throws IOException
    {
        boolean lostPacket = false;
        int retry = 0;

        byte[] receiveData = new byte[sendData.length];

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        do
        {
            DatagramSocket serversocket = new DatagramSocket();

            //Server: sending packet
            serversocket.send(sendPacket);


            // START TIMER
            serversocket.setSoTimeout(timeout);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try
            {
                serversocket.receive(receivePacket);


                int receivedAck = Helper.byteArray2Int(receivePacket.getData());
                if (receivedAck == expectedAck)
                {
                    lostPacket = false;
                    retry = 0;
                    expectedAck = (expectedAck + 1) % 2;
                }

            }
            catch (Exception e)
            {
                packetLoss++;
                lostPacket = true;
            }
            serversocket.close();
        }
        while (lostPacket == true);
        packet++;
    }
}
