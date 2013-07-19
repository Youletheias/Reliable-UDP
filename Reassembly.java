/**
 * @author Yousra
 */
import java.net.*;
import java.io.*;
import java.util.zip.CRC32;
import java.util.Vector;

public class Reassembly {

    private DatagramSocket socket;
    private String fileName;
    private int size;
    private int port;
    private InetAddress address;

    public Reassembly(String fileName, int size, DatagramSocket socket, InetAddress address, int port) {
        this.fileName = fileName;
        this.size = size;
        this.socket = socket;
        this.address = address;
        this.port = port;
    }

    public void reassembleAndStore(int serverOrClient) throws Exception {
        byte[] buffer;
        byte[] checkSumReceived = new byte[8];
        byte[] sequenceNumReceived = new byte[4];
        CRC32 checkSum = new CRC32();
        Vector<byte[]> reassembledFile = new Vector<byte[]> (1);

        byte[] ack = new byte[1];
        ack[0] = new Integer(1).byteValue();
        for (int i = 0; i < size; i = i + Fragmentation.FRAG_SIZE) {
            byte[] fragment;
            if (i + Fragmentation.FRAG_SIZE > size) {
                fragment = new byte[size - i + 12];
            } else {
                fragment = new byte[Fragmentation.FRAG_SIZE + 12];
            }

            DatagramPacket receivePacket = new DatagramPacket(fragment, fragment.length);
            socket.receive(receivePacket);

            buffer = new byte[fragment.length - 12];
            System.arraycopy(fragment, 0, buffer, 0, buffer.length);
            System.arraycopy(fragment, buffer.length, checkSumReceived, 0, 8);
            System.arraycopy(fragment, buffer.length + 8, sequenceNumReceived, 0, 4);

            checkSum.update(buffer);
            long checkSumValue = checkSum.getValue();
            if ( checkSumValue == Fragmentation.toLong(checkSumReceived) ) {
                socket.send(new DatagramPacket(ack, ack.length, address, port));
                int position = Fragmentation.toInt(sequenceNumReceived);
                try {
                    reassembledFile.get(position);
                } catch (Exception ex) {
                    reassembledFile.add(position, buffer);
                }
            }
        }

        String folder;
        if (serverOrClient == 1)
            folder = "server/";
        else
            folder = "client/";
        FileOutputStream fileOut = new FileOutputStream(folder + fileName);
        for (int i = 0; i < reassembledFile.size(); i++) {
            fileOut.write(reassembledFile.get(i), 0, reassembledFile.get(i).length);
        }
        
        fileOut.close();
        
        System.out.println("*********** Check if received :) ************");
    }
}
