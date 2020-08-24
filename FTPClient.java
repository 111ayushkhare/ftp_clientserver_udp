import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.net.SocketException;

public class FTPClient {
    static{
        System.out.println("Default address is localhost and Port number in use is 8954");
        System.out.println("If you want to change it pass it as command line argument");
        System.out.println("As: java FTPServer <IpAddress> <PortNo>");
    }
    public static void main(String[] args) throws IOException {

        // Get port number
        int port = 8954;   //same Port number as in server
        int bufsize = 512;
        final int timeout = 1500;
        if(args.length != 0) {
            port = Integer.valueOf(args[1]);
        }
        // Get an ip address of localhost or the server
        InetAddress ip = InetAddress.getByName("localhost");
        if(args.length != 0) {
            ip=InetAddress.getByName(args[0]); // from command line argument
        }
        System.out.println("$Client: Client activated...\n");

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
            if(echoPrint.startsWith("sending")) {

                System.out.println("Recieving");
                String filename = echoPrint.substring(8);
                int index = filename.lastIndexOf("/");
                filename = filename.substring(index + 1);


                String currentDirectory = System.getProperty("user.dir");
                    //FileWriter fw = new FileWriter(currentDirectory + "/" + "IIIT"+ filrname);
                FileOutputStream fout=new FileOutputStream(currentDirectory + "/" +"IIIT"+ filename);
                DatagramPacket filereceived = new DatagramPacket(new byte[bufsize], bufsize);
                while (true) { // read loop
                    try{
                    filereceived.setLength(bufsize);  // max received packet size
                            socketClient.receive(filereceived);          // the actual receive operation
//                    } catch (SocketTimeoutException ste) {    // receive() timed out
//                        System.err.println("Response timed out!");
//                        continue;
                    } catch (IOException ioe) {                // should never happen!
                        System.err.println("Bad receive");
                        break;
                    }
                        String stri = new String(filereceived.getData(), 0, filereceived.getLength());
                        //System.out.print(stri);        // newline must be part of str
                    byte b[]=stri.getBytes();
                    fout.write(b);

                    }
                    fout.close();
                    System.out.println("Success...");
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

}
