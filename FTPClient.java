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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FTPClient {

    static ArrayList<byte[]> packets = new ArrayList<byte[]>();
    static int previousAck = 0;
    static boolean flag = false;
    public static final int Seg_size_max = 508;

    static final String CLIENT_SIGNATURE = "FTP_Client> ";

    // Static block running at the very beginning of the program
    static {
        System.out.println(CLIENT_SIGNATURE + "Default address is localhost and Port number in use is 8954");
        System.out.println(CLIENT_SIGNATURE + "If you want to change it pass it as command line argument");
        System.out.println(CLIENT_SIGNATURE + "As: java FTPServer <IpAddress> <PortNo>");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        System.out.println(CLIENT_SIGNATURE + "Client activated...\n");

        // Setting port a fixed pre-defind port number
        int port = 8954; // same Port number as in server

        // Setting port number to that value provided in command line arguments (If
        // provied)
        if (args.length != 0) {
            port = Integer.valueOf(args[1]);
        }

        // Setting buffer size for byte arrays used ahead in transmission
        int bufsize = 1024;

        // Setting timeout value
        final int timeout = 1500;

        // Get ip address of localhost or the server
        InetAddress ip = InetAddress.getByName("localhost");

        // Setting IP address to that value if provided in command line arguments
        if (args.length != 0) {
            ip = InetAddress.getByName(args[0]);
        }

        // Intitializing datagram socket
        DatagramSocket socketClient = new DatagramSocket();

        try {
            // Starting the timer for set value
            socketClient.setSoTimeout(0); // listening forever [will alter it in case of file transfer]
        } catch (SocketException e) {
            // Printing out error message
            System.err.println(CLIENT_SIGNATURE + "Socket exception: Timeout not set!");
        }

        // Declaring a datagram packet
        DatagramPacket packetClient;

        // Declaring a byte array
        byte[] bufClient;

        Scanner input = new Scanner(System.in);

        countinuelable: while (true) {
            System.out.print(CLIENT_SIGNATURE);

            // Here client enters its message
            String str = input.nextLine();

            // Converting message to bytes
            bufClient = str.getBytes();
            // Sending request to the server
            packetClient = new DatagramPacket(bufClient, bufClient.length, ip, port);
            socketClient.send(packetClient);

            // Checking if the client wants to stop
            if (str.equals("stop")) {
                System.out.println(CLIENT_SIGNATURE + "You entered \'stop\'");
                System.out.println(CLIENT_SIGNATURE + "Client deactivated...");
                break;
            }
            // Recieving response (ECHO) from server
            byte[] recieveData = new byte[bufsize];
            DatagramPacket echoRecieved = new DatagramPacket(recieveData, recieveData.length);
            socketClient.receive(echoRecieved);

            // Converting recieved byte data into String
            String echoPrint = new String(echoRecieved.getData()).trim();
            if (echoPrint.startsWith("sending")) {
                String filename = echoPrint.substring(8);

                if (filename.startsWith("error")) {
                    System.out.println(CLIENT_SIGNATURE + filename);

                } else {
                    /**
                     * If you choose option 1 then transmission will occur following Stop an Wait
                     * protocol If you choose option 2 or any other type digit then transmission
                     * will occur following Go-back n protocolol
                     * 
                     */
                    System.out.println("Choose protocol type (select 1 or 2)\n1) Stop & Wait \n2)Go back N");
                    String method = input.nextLine();
                    method = method.trim();
                    System.out.println(method + " method");
                    socketClient.send(new DatagramPacket(method.getBytes(), method.getBytes().length, ip, port));

                    System.out.println(CLIENT_SIGNATURE + "Downloading...");
                    int index = filename.lastIndexOf("/");
                    filename = filename.substring(index + 1);
                    /*
                     * Implementation of stop and wait protocol
                     */
                    if (method.charAt(0) == '1') {
                        int ack = 0;
                        // **************************************************//
                        // initializing ack as 0 and will be altered further //
                        // ***************************************** *******//
                        byte[] previousData = new byte[512];
                        while (true) {
                            byte[] receiveData = new byte[512];
                            byte[] sendData = new byte[512];

                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                            // ************************************************************//
                            // In the try & catch code block //
                            // checking if packet is received or not //
                            // if not, handling it with catch block with timeout message //
                            // and closing the socket //
                            // ***********************************************************//
                            try {
                                socketClient.receive(receivePacket);
                                socketClient.setSoTimeout(timeout); // set timeout in milliseconds

                            } catch (Exception e) {
                                System.out.println(CLIENT_SIGNATURE + "Timeout occured\n exiting");
                                packets = new ArrayList<byte[]>();
                                continue countinuelable;
                            }

                            byte[] receivedData = receivePacket.getData();
                            // **************************************************//
                            // getting IpAddress and port from recieved port //
                            // ***************************************** *******//
                            InetAddress IPAddress = receivePacket.getAddress();
                            port = receivePacket.getPort();

                            // **************************************************//
                            // send acknoledgement to server //
                            // ***************************************** *******//
                            sendData = int2ByteArray(ack);
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                            socketClient.send(sendPacket);
                            sendData = new byte[512]; // reseting sendData
                            // **************************************************//
                            // comparing previousData and recievedData //
                            // getting length of recieved data with help of //
                            // byteArray2Int function defined at the bottom //
                            // ***************************************** *******//
                            if (!Arrays.equals(receivedData, previousData)) {
                                int length = byteArray2Int(new byte[] { receivedData[508], receivedData[509],
                                        receivedData[510], receivedData[511] });
                                packets.add(Arrays.copyOfRange(receivedData, 0, length));
                                // ************************************************//
                                // updating previousAck and ack //
                                // this updated ack will be sent in next itration //
                                // ************************************************//
                                previousAck = ack;
                                ack = (ack + 1) % 2; // updating ack [Stop and wait Protocol]
                                // ************************************************//
                                // if length of recieved data is less than Seg_size_max //
                                // that means that is last packet so breaking loop //
                                // ************************************************//
                                if (length < Seg_size_max)
                                    break;
                            } else {
                                ack = previousAck; // else simply updating ack
                            }
                            previousData = receivedData; // updating previousData with recievedData for next time
                        }
                        /**
                         * Using try block to write a file in the directory and also checking if already
                         * exists or not if directory doesn't exists, then handling it with catch block
                         * with Problem occured message
                         */
                        try {
                            String currentDirectory = System.getProperty("user.dir");
                            File file = new File(currentDirectory + "/" + filename); // creating object of File class
                                                                                     // named
                                                                                     // as file
                            FileOutputStream fout; // creating object of FileOutputStream class as fout
                            if (file.exists() && file.isFile()) { // checking if already exists and that is file or
                                                                  // not(folder)
                                System.out.println(CLIENT_SIGNATURE
                                        + "File already exists, do you want to overwrite?\npress Y for yes\npress any key for no\n");
                                String desire = input.nextLine(); // providing option for overwriting or new in case of
                                                                  // conflict
                                if (desire.equals("Y")) {
                                    fout = new FileOutputStream(currentDirectory + "/" + filename);
                                } else {
                                    System.out.print(CLIENT_SIGNATURE + "Enter new file name: ");
                                    String newfilename = input.nextLine();
                                    fout = new FileOutputStream(currentDirectory + "/" + newfilename);
                                }
                            } else {
                                fout = new FileOutputStream(currentDirectory + "/" + filename);
                            }
                            flag = true;
                            for (int i = 0; i < packets.size(); i++) {
                                fout.write(packets.get(i)); // writing everything to file
                            }
                            fout.close(); // closing fout
                            packets = new ArrayList<byte[]>();
                            System.out.println(CLIENT_SIGNATURE + "Downloaded requested file successfully");
                            continue countinuelable;
                        } catch (Exception ex) {
                            System.out.println(CLIENT_SIGNATURE + "Problem occured writing to file");
                        }
                    } else {
                        byte[] receivedData = new byte[Seg_size_max+83];

                        int ExpectedAck = 0;

                        ArrayList<RDTPacket> received = new ArrayList<RDTPacket>();

                        boolean isLast = false;

                        while (!isLast) {

                            System.out.println("Waiting for packet");

                            // Receive packet
                            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                            socketClient.receive(receivedPacket);

                            // Unserialize to a RDTPacket object
                            RDTPacket packet = (RDTPacket) byteToObject(receivedPacket.getData());

                            System.out.println("Successfully recieved packet with sequence " + packet.getSeq());

                            if (packet.getSeq() == ExpectedAck && packet.isLast()) {

                                ExpectedAck++;
                                received.add(packet);

                                System.out.println("Last packet received");

                                isLast = true;

                            } else if (packet.getSeq() == ExpectedAck) {
                                ExpectedAck++;
                                received.add(packet);
                                System.out.println("Packed stored in buffer array");
                            } else {
                                System.out.println("Packet discarded (not in order)");
                            }

                            // Create an RDTAck object
                            RDTAck ackObject = new RDTAck(ExpectedAck);

                            // Serialize
                            byte[] ackBytes = objectToBytes(ackObject);

                            DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length,
                                    receivedPacket.getAddress(), receivedPacket.getPort());

                            socketClient.send(ackPacket);

                            System.out.println(
                                    "Sending ACK to seq " + ExpectedAck + " with " + ackBytes.length + " bytes");

                        }
                        /**
                         * Using try block to write a file in the directory and also checking if already
                         * exists or not if directory doesn't exists, then handling it with catch block
                         * with Problem occured message
                         */
                        try {
                            String currentDirectory = System.getProperty("user.dir");
                            File file = new File(currentDirectory + "/" + filename); // creating object of File class
                                                                                     // named
                                                                                     // as file
                            FileOutputStream fout; // creating object of FileOutputStream class as fout
                            if (file.exists() && file.isFile()) { // checking if already exists and that is file or
                                                                  // not(folder)
                                System.out.println(CLIENT_SIGNATURE
                                        + "File already exists, do you want to overwrite?\npress Y for yes\npress any key for no\n");
                                String desire = input.nextLine(); // providing option for overwriting or new in case of
                                                                  // conflict
                                if (desire.equals("Y")) {
                                    fout = new FileOutputStream(currentDirectory + "/" + filename);
                                } else {
                                    System.out.print(CLIENT_SIGNATURE + "Enter new file name: ");
                                    String newfilename = input.nextLine();
                                    fout = new FileOutputStream(currentDirectory + "/" + newfilename);
                                }
                            } else {
                                fout = new FileOutputStream(currentDirectory + "/" + filename);
                            }
                            flag = true;
                            // for (int i = 0; i < packets.size(); i++) {
                            // fout.write(packets.get(i)); // writing everything to file
                            // }
                            for (RDTPacket p : received) {
                                for (byte b : p.getData()) {
                                    System.out.print("writing..." + "\r");
                                    fout.write((char) b);
                                    System.out.print(".........." + "\r");

                                }
                            }
                            fout.close(); // closing fout
                            packets = new ArrayList<byte[]>();
                            System.out.println(CLIENT_SIGNATURE + "Downloaded requested file successfully");
                            continue countinuelable;
                        } catch (Exception ex) {
                            System.out.println(CLIENT_SIGNATURE + "Problem occured writing to file");
                        }
                    }

                }
            } else {
                System.out.println(CLIENT_SIGNATURE + "Server\'s response - \n" + echoPrint);
                // printing response from
                // server to console
                // outputStream except file
                // downloading case
            }

            System.out.println();
        }

        // Closing the datagram socket
        socketClient.close();

        input.close();
    }

    /**
     * The unsigned right shift operator '>>' do not use the sign bit to fill the
     * trailing positions. It always fills the trailing positions by 0s.
     * 
     * The signed right shift operator '>>>' uses the sign bit to fill the trailing
     * positions
     * 
     * b = 1111 1111 1111 1111 1111 1111 1100 0100 b >> 1 = 1111 1111 1111 1111 1111
     * 1111 1110 0010 b >>> 1 = 0111 1111 1111 1111 1111 1111 1110 0010
     */

    // Method for converting integer to byte array
    public static byte[] int2ByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    // Method for converting byte array to integer
    public static int byteArray2Int(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (int) (b[i] & 0xFF) << shift;
        }

        return value;
    }

    /**
     * Serialization method where object is being represented as 
     * a sequence of bytes that includes the object's data as well as
     * information about the object's type and 
     * the types of data stored in the object.
     */
    public static byte[] objectToBytes(Object obj) throws IOException {
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
		ObjectOutputStream object_out = new ObjectOutputStream(byte_out);
		object_out.writeObject(obj);
		return byte_out.toByteArray();
	}

    /**
     * Deserialization method where object's byte stream
     * is used to create object in memory
     */
	public static Object byteToObject(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byte_out = new ByteArrayInputStream(bytes);
		ObjectInputStream object_out = new ObjectInputStream(byte_out);
		return object_out.readObject();
	}

}