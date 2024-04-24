package bgu.spl.net.impl.tftp;
import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
public class Listener implements Runnable{
    private final Socket sock;
    private BlockingQueue<byte[]> messageQueue;
    private String host;
    private MessageEncoderDecoder<byte[]> encoderDecoder;
    private final int CAPACITY = 512;
    private final int RRQ = 1;
    private final int WRQ = 2;
    private final int DATA = 3;
    private final int ACK = 4;
    private final int ERROR = 5;
    private final int DIRQ = 6;
    private final int BCAST = 9;
    private final int DISC = 10;
    private KeyboardListener keyboardListener;
    private int port;
    private TftpServerProtocol serverProtocol;
    public Listener(BlockingQueue<byte[]> messageQueue, Socket sock,MessageEncoderDecoder<byte[]> encoderDecoder){
        this.messageQueue = messageQueue;
        this.sock = sock;
        this.encoderDecoder = encoderDecoder;
        this.serverProtocol = new TftpServerProtocol();
    }
    @Override
    public void run() {
        try (BufferedInputStream in = new BufferedInputStream(sock.getInputStream());) {
            while(!Thread.currentThread().isInterrupted()) {
                int read;
                while ((read = in.read()) >= 0){
                    byte[] nextMessage = encoderDecoder.decodeNextByte((byte) read);
                    if (nextMessage != null) {
                        byte[] answer = serverProtocol.process(nextMessage);
                        if(answer != null){

                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private Queue<byte[]> handleWrite(String fileName) {
        Queue<byte[]> dataQueue = new LinkedList<>();
        try{
            byte[] fileData = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "\\" + fileName));
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
    private Queue<String> getFileNames(LinkedList<ByteBuffer> list) {
        Queue<String> fileNames = new LinkedList<>();
        ArrayList<Byte> fileName = new ArrayList<>();
        for (ByteBuffer buffer : list) {
            for(int i = 6; i < buffer.capacity(); i++){
                if(buffer.get(i) == 0){
                    fileNames.add(new String(arrayListToArray(fileName), StandardCharsets.UTF_8));
                    fileName.clear();
                }
                else{
                    fileName.add(buffer.get(i));
                }
            }
        }
        return fileNames;
    }
    private byte[] arrayListToArray(ArrayList<Byte> list){
        byte[] arr = new byte[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
    public void setKeyboardListener(KeyboardListener keyboardListener, Socket socket){
        this.keyboardListener = keyboardListener;
        this.keyboardListener.setSocket(socket);

    }
}