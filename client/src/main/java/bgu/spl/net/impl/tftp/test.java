package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class test {

    public static int CAPACITY = 512;
    public static void main(String[] args) throws InterruptedException {
        Queue<byte[]> q = handleWrite("B");
        System.out.println(Arrays.toString(q.remove()));
        System.out.println(Arrays.toString(q.remove()));
    }

    private static Queue<byte[]> handleWrite(String fileName) {
        Queue<byte[]> dataQueue = new LinkedList<>();
        try{
            byte[] fileData = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "\\" + fileName));
            System.out.println(fileData.length);
            int dataLength = fileData.length;
            int offset = 0 ;
            while(dataLength/CAPACITY > 0){
                ByteBuffer currentChunk = ByteBuffer.allocate(CAPACITY);
                currentChunk.put(fileData, offset, CAPACITY);
                dataQueue.add(currentChunk.array());
                currentChunk.clear();
                offset += CAPACITY;
                dataLength -= CAPACITY;
            }
            ByteBuffer lastChunk = ByteBuffer.allocate(dataLength);
            lastChunk.put(fileData, offset, dataLength);
            dataQueue.add(lastChunk.array());
        }
        catch (IOException e){

        }
        return dataQueue;
    }
}
