
/**
 * @author Yousra
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    public static void main(String[] args) {
        int port = 9001;
        DatagramSocket socket = null;
        InetAddress address = null;
        int clientPort = 0;

        try {
            socket = new DatagramSocket(port);
            while (true) {
                DatagramPacket receivePacket = null;
                try {
                    byte[] data = new byte[100];

                    System.out.println("Waiting for connection...");

                    receivePacket = new DatagramPacket(data, data.length);
                    socket.receive(receivePacket);

                    address = receivePacket.getAddress();
                    clientPort = receivePacket.getPort();

                    System.out.println("Datagram received from: \n" + "Host: " + address + "\nPort: " + clientPort);

                    String message = new String(receivePacket.getData());
                    StringTokenizer tokenizer = new StringTokenizer(message, " ");
                    String request = tokenizer.nextToken();
                    String fileName = tokenizer.nextToken();
                    String feedback;

                    if (request != null && request.equalsIgnoreCase("Download")) {
                        FileInputStream fileIn = new FileInputStream("server/" + fileName);
                        int size = fileIn.available();

                        if (size > 9000000) {
                            feedback = "File too large\n";
                            System.out.println(feedback);
                            byte[] feedbackToClient = feedback.getBytes();
                            DatagramPacket feedbackPacket = new DatagramPacket(feedbackToClient, feedbackToClient.length, address, clientPort);
                            socket.send(feedbackPacket);
                        } else {
                            feedback = "You want to download " + fileName + " of size: " + fileIn.available();
                            byte[] feedbackToClient = feedback.getBytes();
                            DatagramPacket feedbackPacket = new DatagramPacket(feedbackToClient, feedbackToClient.length, address, clientPort);
                            socket.send(feedbackPacket);
                            System.out.println("Feedback to client: " + new String(feedbackPacket.getData()));

                            byte[] buffer = new byte[size];
                            fileIn.read(buffer);
                            fileIn.close();

                            Fragmentation frag = new Fragmentation(buffer, socket, address, clientPort);
                            frag.segmentAndSend();

                            System.out.println("*********** " + fileName + " sent successfully :) ************");
                        }
                    } else if (request != null && request.equalsIgnoreCase("Upload")) {
                        int size = (int) Double.parseDouble(tokenizer.nextToken());

                        if (size > 9000000) {
                            feedback = "File too large\n";
                            System.out.println(feedback);
                            byte[] feedbackToClient = feedback.getBytes();
                            DatagramPacket feedbackPacket = new DatagramPacket(feedbackToClient, feedbackToClient.length, address, clientPort);
                            socket.send(feedbackPacket);
                        } else {
                            
                            feedback = "You want to upload " + fileName + " of size: " + size;
                            byte[] feedbackToClient = feedback.getBytes();
                            DatagramPacket feedbackPacket = new DatagramPacket(feedbackToClient, feedbackToClient.length, address, clientPort);
                            socket.send(feedbackPacket);

                            System.out.println("Feedback to client: " + new String(feedbackPacket.getData()));

                            Reassembly rea = new Reassembly(fileName, size, socket, InetAddress.getLocalHost(), clientPort);
                            rea.reassembleAndStore(1);
                        }
                    } else {
                        feedback = "Type [Download/Upload] [One space] [File name] and hit Enter";
                        byte[] feedbackToClient = feedback.getBytes();
                        DatagramPacket feedbackPacket = new DatagramPacket(feedbackToClient, feedbackToClient.length, address, clientPort);
                        socket.send(feedbackPacket);
                    }

                } catch (FileNotFoundException ex) {
                    System.out.println("File NOT Found");
                    String feedback = "File NOT Found\n";
                    byte[] feedbackToClient = feedback.getBytes();
                    DatagramPacket feedbackPacket = new DatagramPacket(feedbackToClient, feedbackToClient.length, address, clientPort);
                    try {
                        socket.send(feedbackPacket);
                    } catch (IOException ex1) {
                        System.out.println("An I/O Exception occured. Please retry.");
                    }
                } catch (IOException ex) {
                    System.exit(0);
                } catch (Exception ex) {
                    System.out.println("An Exception occured. Please retry.");
                }
            }
        } catch (SocketException ex) {
            System.out.println("Could not bind to port!\n");
        }
    }
}
