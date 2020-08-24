import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.io.FileInputStream;

public class FTPServer {
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
        int bufsize = 512;

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
                byte[] buffer = new byte[bufsize];
                DatagramPacket fileContent = new DatagramPacket(new byte[0], 0, IPAddress, port);
                while (fileIn.read(buffer) != -1) {
                    int slen;
                    slen = buffer.length;
                    //byte[] bbuf = buffer.getBytes();

                    fileContent.setData(buffer);
                    fileContent.setLength(slen);
                    System.out.println(buffer);
                    try {
                        socketServer.send(fileContent);
                    }
                    catch (IOException ioe) {
                        System.err.println("send() failed");
                        return;
                    }
                }

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
}
