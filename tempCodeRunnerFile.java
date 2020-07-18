// Send byte array
            byte[] bufSendServer = new byte[1024];
            String responseString = requestString.toString();
            bufSendServer = responseString.getBytes();

            // Response by sending message back to Server
            DatagramPacket sendPacketServer = new DatagramPacket(bufSendServer,bufSendServer.length,ipServer,9999);
            socketServer.send(sendPacketServer);