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

        System.out.println(SERVER_SIGNATURE + "Server started...\n");

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
            // triming trailing spaces in request String
            requestString=requestString.trim();
            // Printing out received message
            System.out.println(requestString);

            // Setting IP address
            InetAddress IPAddress = packetServer.getAddress();
            
            // Setting port number 
            int port = packetServer.getPort();

            StringBuilder sb = new StringBuilder("\n");

            // Checking if request message from client equals "ls"
            if (requestString.equals("ls")) {

                String currentDirectory = System.getProperty("user.dir");
                File directoryPath=new File(currentDirectory);
                File fileList[] = directoryPath.listFiles();
                for (File file: fileList)
                    sb.append(file+"\n");

            } 
            // Checking if request message from client equals "cd"
            else if(requestString.startsWith("cd ")) {
                String command = requestString.substring(3)+"/";
                File directoryPath=new File(command);
                File fileList[] = directoryPath.listFiles();
                for (File file: fileList)
                    sb.append(file+"\n");
            } 
            // Checking if request message from client equals "file"
            else if(requestString.startsWith("get")) {

                /*
                 * Implementation of stop and wait protocol                    
                */

                

                   //*******************************************************//
                  //                     get <filename>                   //
                 //  In above formate request will come from client side  //
                //*******************************************************//

                   //******************************************************//
                  //              In below line                          //
                 //  scrapping the filename from requested String       //
                //*****************************************************//
                String TobeSent = requestString.substring(4);
                
                
                //******************************************************//
                //              in below line                           //
                //  creating a mutable string with the help of         //
                //  StringBuilder class, in which we will appned      //
                //  information that server is going to send         //
                //**************************************************//
                StringBuilder filesb = new StringBuilder("\n");
                
                
                //******************************************************//
                //              In below line                           //
                //  creating object of FileInputStream   to work with  //
                //  files and folders in System                       //
                //***************************************************//
                FileInputStream fileIn;
                
                
                //*********************************************************//
                //              in above try & catch code block           //
                //  checking if requested file exists or not              //
                // if file does't exists, handling it with catch block    //
                // continueing the loop from start again with             //
                // continue lable                                         //
                // if exists fileIn objects is created and will use latter//
                //********************************************************//
                try {
                    fileIn = new FileInputStream("./" + TobeSent);
                    filesb.append("sending ");
                    filesb.append(TobeSent);
                    sendData = (filesb.toString()).getBytes();
                    DatagramPacket echoPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    socketServer.send(echoPacket);
                }
                catch (Exception e) {
                    filesb.append("sending ");
                    filesb.append("error : No such file present in server's directory ");
                    sendData = (filesb.toString()).getBytes();
                    DatagramPacket echoPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    socketServer.send(echoPacket);
                    filesb=new StringBuilder("\n");
                    bufServer = new byte[1024];   // resetting bufServer for next Iteration
                    continue continuelable;
                }
                
                //*******************************************************//
                //              In below 4 lines of code                 //
                //  creating a byte array named buffer of size 508       //
                // creating a byte array named sizebyte                  //
                // defining and initializing size and sumbyte of int type//
                //*******************************************************//
                byte[] buffer = new byte[508];
                byte[] sizebyte = int2ByteArray(512);
                int size=0;
                int sumbyte=0;
                

                //*******************************************************//
                // starting while loop for sending file to client       //
                // reading fileIn and storing it to buffer array and    //
                // storing size of read data in size variable          //
                //*****************************************************//
                while ((size = fileIn.read(buffer)) >= 0) {
                    //****************************************************//
                    // converting size variable to byteArray with help of //
                    // function defined at the end of this file , and     //
                    //  storing it to sizebyte variable                  //
                    //***************************************************//
                    sizebyte=int2ByteArray(size);
                    

                    //****************************************************//
                    //             In the below lines of code                //
                    // increasing the array size of buffer by copying    //
                    //  it to buffer array with size 512                 //
                    //  and at last 4 elements of array copying the     //
                    //  sizebyte array  with for loop                    //
                    //***************************************************//
                    buffer=Arrays.copyOf(buffer,512);
                    for(int i=0;i<4;i++)
                    {
                        buffer[508+i] = sizebyte[i];

                    }

                    //***************************************************//
                    // creating boolean named lostPacket to track wheather //
                    // packet lost or not                                 //
                    //***************************************************//
                    boolean lostPacket = false;
                    
                    //*****************************************************//
                    // creating byte Array named recievedData for storing  //
                    // recieved Data                                      //
                    //***************************************************//
                    byte[] receiveData = new byte[buffer.length];
                    
                    DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, IPAddress, port);
                    //*****************************************************//
                    // creating DatagramPacket object named sendPacket    //
                    //***************************************************//

                    //*****************************************************//
                    // starting do while loop for sending packets         //
                    //***************************************************//
                    do
                    {


                        socketServer.send(sendPacket);
                        socketServer.setSoTimeout(timeout);
                        //*****************************************************//
                        // sending Packet and setting timeout                 //
                        //***************************************************//
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        //*****************************************************//
                        // creating DatagramPacket object named recievePacket //
                        //***************************************************//
                        try
                        {
                            //*****************************************************//
                            // trying to recieve Packet in this try block         //
                            //***************************************************//
                            socketServer.receive(receivePacket);
                            
                            //*****************************************************//
                            // if successfully recieved then comparing it with    //
                            // expected acknoledgement if equal then incrementing // 
                            //acknoledgement by one and taking modulo 2 of that  //
                            //***************************************************//
                            int receivedAck = byteArray2Int(receivePacket.getData());
                            if (receivedAck == expectedAck)
                            {
                                lostPacket = false;
                                expectedAck = (expectedAck + 1) % 2;
                                //**************************************************//
                                // calculating sumbyte to show in console how      //
                                // many bytes has been sent                        //
                                //***************************************** *******//
                                sumbyte+=size;
                                System.out.print("sent "+humanize(sumbyte)+" to client"+"\r");
                                
                            }

                        }
                        catch (Exception e)
                        {
                            //**************************************************//
                            // if recieving fails that means packet lost       //
                            // incrementing packetloss and updating lostPacket //
                            //***************************************** *******//
                            packetLoss++;
                            lostPacket = true;
                        }
                    }
                    //******************************************************//
                    // this while is part of do whaile loop that we started //
                    //***************************************** ***********//
                    while (lostPacket == true);
                    packet++;
                    buffer = new byte[508];
                }
                //**************************************************//
                // Closing filein after  reading completed         //
                //***************************************** *******//
                fileIn.close();
                System.out.println("\r");

                System.out.println("done");
                bufServer = new byte[1024]; // resetting bufServer for next Iteration
                continue continuelable;

            } else {
                // If Client types command other than 'file , ls, cd '
                sb.append("Unknown Command");
            }




            // Converting data to bytes
            sendData = (sb.toString()).getBytes();

            // Checking if Client wants to stop
            if (requestString.equals("stop")) {
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

/**
     * The unsigned right shift operator '>>' do not use the sign bit 
     * to fill the trailing positions.
     * It always fills the trailing positions by 0s.
     * 
     * The signed right shift operator '>>>' uses the sign bit
     * to fill the trailing positions
     * 
     * b = 1111 1111 1111 1111 1111 1111 1100 0100
     * b >> 1 = 1111 1111 1111 1111 1111 1111 1110 0010
     * b >>>  1 = 0111 1111 1111 1111 1111 1111 1110 0010
     */

    // Method for converting integer to byte array  
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

    // Method for converting byte array to integer 
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
    public static String humanize(int n)
    {
        String str=new String("");
        if(n<1024)
        {
            str=n+" bytes";
        }
        if(n<(1024*1024))
        {
            str=(n/1024)+" kb";
        }
        else{
            str=(n/(1024*1024))+" mb";
        }
        return str;
    }
}

