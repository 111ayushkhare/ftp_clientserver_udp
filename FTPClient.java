import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.util.*;
import java.io.*;


public class FTPClient {
    static ArrayList<byte[]> packets = new ArrayList<byte[]>();
    static int previousAck = 0;
    static boolean flag=false;
    static{
        System.out.println("Default address is localhost and Port number in use is 8954");
        System.out.println("If you want to change it pass it as command line argument");
        System.out.println("As: java FTPServer <IpAddress> <PortNo>");
    }
    public static void main(String[] args) throws IOException {

        // Get port number
        int port = 8954;   //same Port number as in server
        int bufsize = 1024;
        final int timeout = 1500;
        if(args.length != 0) {
            port = Integer.valueOf(args[1]);
        }
        // Get an ip address of localhost or the server
        InetAddress ip = InetAddress.getByName("localhost");
        if(args.length != 0) {
            ip=InetAddress.getByName(args[0]); // from command line argument
        }
        System.out.println("Client> Client activated...\n");

        // Get a datagram socket
        DatagramSocket socketClient = new DatagramSocket();

        try {
            socketClient.setSoTimeout(timeout);       // set timeout in milliseconds
        } catch (SocketException e) {
            System.err.println("socket exception: timeout not set!");
        }
        // Get a datagram packet
        DatagramPacket packetClient;




        byte[] bufClient;

        Scanner input = new Scanner(System.in);
        countinuelable:
        while(true) {
            System.out.print("Client> ");

            // Enter your message
            String str = input.nextLine();
            // Converting message to bytes
            bufClient = str.getBytes();
            System.out.println(str);
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
            byte[] recieveData = new byte[bufsize];
            DatagramPacket echoRecieved = new DatagramPacket(recieveData, recieveData.length);
            socketClient.receive(echoRecieved);

            // Converting recieved byte data into String
            String echoPrint = new String(echoRecieved.getData()).trim();
            if(echoPrint.startsWith("sending")) {

                System.out.println("Recieving");
                String filename = echoPrint.substring(8);
                int index = filename.lastIndexOf("/");
                filename = filename.substring(index + 1);


                int ack = 0;
                byte[] previousData = new byte[512];
                while (true)
                {
                    byte[] receiveData = new byte[512];
                    byte[] sendData = new byte[512];

                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    try
                    {
                        socketClient.receive(receivePacket);
                    }
                    catch (Exception e)
                    {
                        System.out.println("timeout occured\n exiting");
                        socketClient.close();

                        break;
                    }

                    byte[] receivedData = receivePacket.getData();

                    InetAddress IPAddress = receivePacket.getAddress();
                    port = receivePacket.getPort();


                    // sending Ack to server..........
                    sendData = int2ByteArray(ack);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    socketClient.send(sendPacket);
                    sendData = new byte[512];

                    if (!Arrays.equals(receivedData, previousData))
                    {
                        int length = byteArray2Int(new byte[] {receivedData[508], receivedData[509], receivedData[510], receivedData[511]});
                        packets.add(Arrays.copyOfRange(receivedData,0, length));
                        previousAck = ack;
                        ack = (ack + 1) % 2;
                        if (length < 508)
                            break;
                    }
                    else
                    {
                        ack = previousAck;
                    }
                    previousData = receivedData;


                }
                try
                {
                    String currentDirectory = System.getProperty("user.dir");
                    File file = new File(currentDirectory+"/"+filename);
                    FileOutputStream fout;
                    boolean exists = file.exists();
                    if (file.exists() && file.isFile())
                    {
                        System.out.println("file already exists, do you want to overwrite?\npress Y for yes\npress any key for no\n");
                        String desire = input.nextLine();
                        if(desire.equals("Y"))
                        {
                            fout=new FileOutputStream(currentDirectory + "/"+ filename);
                        }
                        else{
                            String newfilename = input.nextLine();
                            fout=new FileOutputStream(currentDirectory + "/"+ newfilename);
                        }
                    }
                    else
                    {
                        fout=new FileOutputStream(currentDirectory + "/"+ filename);
                    }
                    flag=true;
                    for (int i = 0; i < packets.size(); i++)
                    {
                        fout.write(packets.get(i));
                    }
                    fout.close();
                    System.out.println("Downloaded requested file successfully");
                    continue countinuelable;
                }
                catch (Exception ex)
                {
                    System.out.println("Problem occured writing to file");

                }
            }
            else
            {
                System.out.println("$Client: Server\'s response - " + echoPrint);

            }



            System.out.println();
        }

        // Closing the datagram socket
        socketClient.close();

        input.close();
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
