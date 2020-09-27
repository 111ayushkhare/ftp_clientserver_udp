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

    static final String SERVER_SIGNATURE = "FTP_Server> ";

    // Static block running at the very beginning of the program
    static{
        System.out.println(SERVER_SIGNATURE + "Default Port number in use is 8954");
        System.out.println(SERVER_SIGNATURE + "If you want to change it pass it as command line argument");
        System.out.println(SERVER_SIGNATURE + "As: java FTPServer <PortNo>");
    }
    public static void main(String[] args) throws IOException {

        System.out.println("FTP_Server> Server started...\n");

        // Setting port a fixed pre-defind port number
        int listen_port = 8954;

        // Setting port number to that value provided in command line arguments (If provied)
        if(args.length != 0) {
            listen_port = Integer.valueOf(args[0]);
        }

        // Setting buffer size for byte arrays used ahead in transmission
        int bufsize = 1024;

        // Declaring and Intitializing datagram socket
        DatagramSocket socketServer = new DatagramSocket(listen_port);

        // Declaring byte arrays to recieve data
        byte[] bufServer = new byte[bufsize];

        // Declaring byte arrays to recieve data
        byte[] sendData = new byte[bufsize];

        continuelable:
        while(true) {
            System.out.print(SERVER_SIGNATURE);

            // Setting the timer to ZERO
            socketServer.setSoTimeout(0);
            
            // Receiving request from client
            DatagramPacket packetServer = new DatagramPacket(bufServer, bufServer.length);
            socketServer.receive(packetServer);

            // Storing the recieved String from client 
            String requestString = new String(packetServer.getData());

            // Printing out received message
            System.out.println(requestString.trim());

            // Setting IP address
            InetAddress IPAddress = packetServer.getAddress();
            
            // Setting port number 
            int port = packetServer.getPort();

            StringBuilder sb = new StringBuilder("\n");

            // Checking if request message from client equals "ls"
            if (requestString.trim().equals("ls")) {

                String currentDirectory = System.getProperty("user.dir");
                File directoryPath=new File(currentDirectory);
                File fileList[] = directoryPath.listFiles();
                for (File file: fileList)
                    sb.append(file+"\n");

            } 
            // Checking if request message from client equals "cd"
            else if(requestString.trim().startsWith("cd ")) {
                String command = requestString.trim().substring(3)+"/";
                File directoryPath=new File(command);
                File fileList[] = directoryPath.listFiles();
                for (File file: fileList)
                    sb.append(file+"\n");
            } 
            // Checking if request message from client equals "file"
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
                byte[] sizebyte = int2ByteArray(512);
                int size=0;
                int sumbyte=0;
                DatagramPacket fileContent = new DatagramPacket(new byte[0], 0, IPAddress, port);
                while ((size = fileIn.read(buffer)) >= 0) {
                    sizebyte=int2ByteArray(size);
                    sumbyte+=size;
                    System.out.print("sent "+sumbyte+" bytes to client"+"\r");

                    buffer=Arrays.copyOf(buffer,512);
                    for(int i=0;i<4;i++)
                    {
                        buffer[508+i] = sizebyte[i];

                    }
//                    sendSTWT(buffer, IPAddress, port, timeout);
                    boolean lostPacket = false;
                    int retry = 0;

                    byte[] receiveData = new byte[buffer.length];

                    DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, IPAddress, port);
                    do
                    {
//                        DatagramSocket serversocket = new DatagramSocket();

                        socketServer.send(sendPacket);
                        socketServer.setSoTimeout(timeout);

                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        try
                        {
                            socketServer.receive(receivePacket);


                            int receivedAck = byteArray2Int(receivePacket.getData());
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
//                        serversocket.close();
                    }
                    while (lostPacket == true);
                    packet++;
                    buffer = new byte[508];
                }
                fileIn.close();
                System.out.println("\r");

                System.out.println("done");
                continue continuelable;

            } else {
                // If Client types command other than 'file'
                sb.append("Unknown Command");
            }




            // Converting data to bytes
            sendData = (sb.toString()).getBytes();

            // Checking if Client wants to stop
            if (requestString.trim().equals("stop")) {
                System.out.println("FTPServer> Client wants to stop");
                break;
            }

            // Send the response to Client (ECHO)
            DatagramPacket echoPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            socketServer.send(echoPacket);
            System.out.println("FTPServer> Response sent.");

            // Clear buffer after every message;
            bufServer = new byte[1024];

            System.out.println();
            }

        // Closing the server
        System.out.println("FTPServer> Server shuting down...");
        socketServer.close();
    }
    public static byte[] int2ByteArray(int value)
    {
        return new byte[]
                {
                        (byte) (value >>> 24),
                        (byte) (value >>> 16),
                        (byte) (value >>> 8),
                        (byte) value
                };
    }
    public static int byteArray2Int(byte[] b)
    {
        int value = 0;
        for (int i = 0; i < 4; i++)
        {
            int shift = (3 - i) * 8;
            value += (int) (b[i] & 0xFF) << shift;
        }

        return value;
    }
}

