package bgu.spl.net.impl.tftp;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class test {
    public static byte[] combineLinkedList(LinkedList<ByteBuffer> list) {
        return list.stream()                            // Stream of ByteBuffer objects
                .map(ByteBuffer::array)             // Map each ByteBuffer to its array
                .reduce(new byte[0], test::concatArrays);  // Reduce to combine arrays
    }

    private static byte[] concatArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    public static void main(String[] args) throws InterruptedException {
        LinkedList<ByteBuffer> bufferList = new LinkedList<>();
        bufferList.add(ByteBuffer.wrap(new byte[]{1, 2, 3}));
        bufferList.add(ByteBuffer.wrap(new byte[]{4, 5}));
        bufferList.add(ByteBuffer.wrap(new byte[]{6, 7, 8, 9}));

        // Combine ByteBuffer objects into one array using stream
        byte[] combined = combineLinkedList(bufferList);

        // Print combined array
        System.out.println("Combined array:");
        for (byte b : combined) {
            System.out.print(b + " ");
        }
    }
}
