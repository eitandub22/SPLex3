package bgu.spl.net.impl.tftp.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransferHandler {
    private static final int CAPACITY = 512;
    public static byte[] handleDir(Deque<ByteBuffer> dirQueue) {
        if(dirQueue.isEmpty()){
            List<String> fileNames = Stream.of(new File("Files").listFiles())
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toList());
            for(String fileName : fileNames){
                ByteBuffer currentFile = ByteBuffer.allocate(fileName.getBytes().length + 1);
                currentFile.put(fileName.getBytes());
                currentFile.put((byte) 0);
                dirQueue.add(currentFile);
            }
        }
        int capacity = dirQueue.stream().mapToInt(buffer -> buffer.array().length).sum();
        ByteBuffer currentFile = dirQueue.peek();
        ByteBuffer currentData = capacity/CAPACITY > 0 ? ByteBuffer.allocate(CAPACITY) : ByteBuffer.allocate(capacity);
        while(currentData.position() < CAPACITY){
            int oldPos = currentData.position();
            currentData.put(currentFile.array(), 0, Math.min(currentData.capacity() - currentData.position(), currentFile.capacity()));
            if(currentData.position() == oldPos + currentFile.capacity()) dirQueue.remove();
            else{
                byte[] reminder = dirQueue.remove().array();
                reminder = Arrays.copyOfRange(reminder, currentData.capacity() - currentData.position(), reminder.length);
                dirQueue.addFirst(ByteBuffer.wrap(reminder));
            }
        }
        return currentData.array();
    }

    public static byte[] handleRead(Deque<ByteBuffer> readQueue, String filename){
        if(readQueue.isEmpty()){
            try{
                FileInputStream  fileInputStream = new FileInputStream(filename);
                long fileLength = new File(filename).length();
                byte[] currentChunk = fileLength/CAPACITY > 0 ? new byte[CAPACITY] : new byte[(int) fileLength];
                while ((fileInputStream.read(currentChunk)) != -1) {
                    readQueue.add(ByteBuffer.wrap(currentChunk));
                    fileLength -= CAPACITY;
                    currentChunk = fileLength/CAPACITY > 0 ? new byte[CAPACITY] : new byte[(int) fileLength];
                }
                fileInputStream.close();
            }
            catch (IOException e) {
                return null;
            }
        }
        int capacity = readQueue.stream().mapToInt(buffer -> buffer.array().length).sum();
        ByteBuffer currentFile = readQueue.peek();
        ByteBuffer currentData = capacity/CAPACITY > 0 ? ByteBuffer.allocate(CAPACITY) : ByteBuffer.allocate(capacity);
        while(currentData.position() < CAPACITY){
            int oldPos = currentData.position();
            currentData.put(currentFile.array(), 0, Math.min(currentData.capacity() - currentData.position(), currentFile.capacity()));
            if(currentData.position() == oldPos + currentFile.capacity()) readQueue.remove();
            else{
                byte[] reminder = readQueue.remove().array();
                reminder = Arrays.copyOfRange(reminder, currentData.capacity() - currentData.position(), reminder.length);
                readQueue.addFirst(ByteBuffer.wrap(reminder));
            }
        }
        return currentData.array();
    }

    public static boolean handleData(byte[] data, String fileName){
        short packetSize = (short) (((short) data[0]) << 8 | (short) (data[1]));
        short blockNum = (short) (((short) data[2]) << 8 | (short) (data[3]));
        byte[] fileData = Arrays.copyOfRange(data, 4, data.length);
        try{
            Files.write(Paths.get("Files" + "\\" + fileName), fileData);
        }
        catch (IOException exception){
            return false;
        }
        return true;
    }
}