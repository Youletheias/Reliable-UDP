/**
 * @author Yousra
 */
import java.util.*;
import java.util.zip.CRC32;
import java.net.*;

public class Fragmentation {

    private byte[] fileInBytes;
    private DatagramSocket socket;
    private InetAddress address;
    private int clientPort;
    public static int FRAG_SIZE = 10000;

    public Fragmentation(byte[] fileInBytes, DatagramSocket socket, InetAddress address, int clientPort) {
        this.fileInBytes = fileInBytes;
        this.socket = socket;
        this.address = address;
        this.clientPort = clientPort;
    }

    public void segmentAndSend() throws Exception {
        int i;
        byte[] rawFragment, fragment;
        int sequence = 0;
        boolean acknowleged = false;

        CRC32 checkSum = new CRC32();
        for (i = 0; i < fileInBytes.length; i = i + FRAG_SIZE) {
            if (i + FRAG_SIZE > fileInBytes.length) {
                rawFragment = Arrays.copyOfRange(fileInBytes, i, fileInBytes.length);
                checkSum.update(rawFragment);
                long checkSumValue = checkSum.getValue();
                byte[] checkSumByte = Fragmentation.toByteArray(checkSumValue);

                byte[] sequenceNumByte = Fragmentation.toByteArray(sequence);
                byte[] eofByte = Fragmentation.toByteArray(1);

                fragment = new byte[rawFragment.length + checkSumByte.length + sequenceNumByte.length + eofByte.length];
                System.arraycopy(rawFragment, 0, fragment, 0, rawFragment.length);
                System.arraycopy(checkSumByte, 0, fragment, rawFragment.length, checkSumByte.length);
                System.arraycopy(sequenceNumByte, 0, fragment, rawFragment.length + checkSumByte.length, sequenceNumByte.length);
                System.arraycopy(sequenceNumByte, 0, fragment, rawFragment.length + checkSumByte.length + eofByte.length, eofByte.length);

            } else {
                rawFragment = Arrays.copyOfRange(fileInBytes, i, i + FRAG_SIZE);
                checkSum.update(rawFragment);
                long checkSumValue = checkSum.getValue();
                byte[] checkSumByte = Fragmentation.toByteArray(checkSumValue);

                byte[] sequenceNumByte = Fragmentation.toByteArray(sequence);
                byte[] eofByte = Fragmentation.toByteArray(0);

                fragment = new byte[FRAG_SIZE + checkSumByte.length + sequenceNumByte.length + eofByte.length];
                System.arraycopy(rawFragment, 0, fragment, 0, rawFragment.length);
                System.arraycopy(checkSumByte, 0, fragment, FRAG_SIZE, checkSumByte.length);
                System.arraycopy(sequenceNumByte, 0, fragment, FRAG_SIZE + checkSumByte.length, sequenceNumByte.length);
                System.arraycopy(sequenceNumByte, 0, fragment, FRAG_SIZE + checkSumByte.length + eofByte.length, eofByte.length);
            }
            while (!acknowleged) {
                DatagramPacket sendPacket = new DatagramPacket(fragment, fragment.length, address, clientPort);
                socket.send(sendPacket);
                try {
                    socket.receive(new DatagramPacket(new byte[1], 1));
                    sequence++;
                    acknowleged = true;
                } catch (SocketException ex) {
                    // resend and wait for acknowledgement
                }
            }
            acknowleged = false;
        }
    }

    public static byte[] toByteArray(long data) {

        return new byte[]{
                    (byte) ((data >> 56) & 0xff),
                    (byte) ((data >> 48) & 0xff),
                    (byte) ((data >> 40) & 0xff),
                    (byte) ((data >> 32) & 0xff),
                    (byte) ((data >> 24) & 0xff),
                    (byte) ((data >> 16) & 0xff),
                    (byte) ((data >> 8) & 0xff),
                    (byte) ((data >> 0) & 0xff),};

    }

    public static long toLong(byte[] data) {
        if (data == null || data.length != 8) {
            return 0x0;
        }
        return (long) ((long) (0xff & data[0]) << 56
                | (long) (0xff & data[1]) << 48
                | (long) (0xff & data[2]) << 40
                | (long) (0xff & data[3]) << 32
                | (long) (0xff & data[4]) << 24
                | (long) (0xff & data[5]) << 16
                | (long) (0xff & data[6]) << 8
                | (long) (0xff & data[7]) << 0);
    }

    public static byte[] toByteArray(int data) {
        return new byte[]{
                    (byte) ((data >> 24) & 0xff),
                    (byte) ((data >> 16) & 0xff),
                    (byte) ((data >> 8) & 0xff),
                    (byte) ((data >> 0) & 0xff),};
    }

    public static int toInt(byte[] data) {
        if (data == null || data.length != 4) {
            return 0x0;
        }
        return (int) (
                (0xff & data[0]) << 24
                | (0xff & data[1]) << 16
                | (0xff & data[2]) << 8
                | (0xff & data[3]) << 0);
    }
}
