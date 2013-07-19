
/**
 * @author Yousra
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    public static void main(String[] args) {
        int port = 9001;
        DatagramSocket socket = null;

        try {
            String message = args[0] + " " + args[1];
            byte[] feedback = new byte[200];
            byte[] buffer = new byte[1];


            if (args[0].equalsIgnoreCase("Upload")) {
                
                FileInputStream fileIn = new FileInputStream("client/" + args[1]);
                int size = fileIn.available();
                if (size < 9000000) {
                    buffer = new byte[size];
                    fileIn.read(buffer);
                    fileIn.close();
                    message += " " + size;
                } else {
                    message = "File too large";
                    System.out.println(message);
                    }
            }

            byte[] data = message.getBytes();

            socket = new DatagramSocket();

            DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), port);
            socket.send(sendPacket);

            DatagramPacket feedbackPacket = new DatagramPacket(feedback, feedback.length);
            socket.receive(feedbackPacket);

            String feed = new String(feedbackPacket.getData());

            if (feed.startsWith("You want to download")) {
                StringTokenizer tokenizer = new StringTokenizer(feed, ":");
                String str = tokenizer.nextToken();
                str = tokenizer.nextToken().substring(1);

                int size = (int) Double.parseDouble(str);
                String fileName = new String(sendPacket.getData()).substring(9);

                Reassembly rea = new Reassembly(fileName, size, socket, InetAddress.getLocalHost(), port);
                rea.reassembleAndStore(0);
            } else if (feed.startsWith("You want to upload")) {

                Fragmentation frag = new Fragmentation(buffer, socket, InetAddress.getLocalHost(), 9001);
                frag.segmentAndSend();

                System.out.println("*********** " + args[1] + " uploaded successfully :) ************");
            } else if(feed.startsWith("File NOT")){
                System.out.println("File NOT Found");
            }

        } catch(FileNotFoundException ex){
            System.out.println("File NOT Found");
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Type [Download/Upload] [One space] [File name] and hit Enter");
        } catch (java.net.UnknownHostException ex) {
            System.out.println("The server could not be found. Program will now exit.");
        } catch (SocketException ex) {
            System.out.println("The client could not be bound to the randomly chosen port.");
        } catch (IOException ex) {
            System.out.println("An Exception Occured.");
        } catch (NumberFormatException ex) {
            System.out.println("An Exception Occured.");
        } catch (Exception ex) {
            System.out.println("An Exception Occured.");
        }

    }
}
