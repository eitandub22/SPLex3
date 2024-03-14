package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.tftp.packets.PacketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Listener implements Runnable{
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

    public Listener(BlockingQueue<byte[]> messageQueue, String host, int port, MessageEncoderDecoder<byte[]> encoderDecoder){
        this.messageQueue = messageQueue;
        this.host = host;
        this.encoderDecoder = encoderDecoder;
        this.port = port;
    }
    @Override
    public void run() {
        try (Socket sock = new Socket(host, port);
            BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream())) {
            while(!Thread.currentThread().isInterrupted()){
                boolean firstMsg = true;
                Queue<byte[]> writeQueue = new LinkedList<>();
                short writeBlock = 0;
                byte[] currentMessage = messageQueue.take();
                if (currentMessage.length == 1) {
                    switch (currentMessage[0]) {
                        case 0://invalid input error
                            System.out.println("Input is invalid");
                            synchronized (this){
                                this.notifyAll();
                            }
                            continue;
                        case 1://file exists error
                            System.out.println("file already exists");
                            synchronized (this){
                                this.notifyAll();
                            };
                            continue;
                        case 2://file not exists error
                            System.out.println("file does not exists");
                            synchronized (this){
                                this.notifyAll();
                            };
                            continue;
                    }
                }
                short currentOpcode = (short) (((short) currentMessage[0]) << 8 | (short) (currentMessage[1]) & 0x00ff);
                if(currentOpcode == WRQ){
                    String fileName = new String(Arrays.copyOfRange(currentMessage, 2, currentMessage.length - 1));
                    writeQueue = handleWrite(fileName);
                }
                out.write(encoderDecoder.encode(currentMessage));
                out.flush();

                byte[] ansArr = null;
                ByteBuffer answerBuffer = null;
                LinkedList<ByteBuffer> transferedData = new LinkedList<>();
                while(in.available() > 0 || firstMsg){
                    firstMsg = false;
                    
                    while((ansArr = encoderDecoder.decodeNextByte((byte)in.read())) == null){}
                    answerBuffer = ByteBuffer.wrap(ansArr);
                    int answerLength = ansArr.length;
                    short answerOpcode = (short) (((short) answerBuffer.array()[0]) << 8 | (short) (answerBuffer.array()[1]) & 0x00ff);
                    switch (answerOpcode){
                        case DATA:
                            if(answerLength > 0 && answerLength <= CAPACITY){
                                transferedData.add(ByteBuffer.wrap(answerBuffer.array()));
                                out.write(encoderDecoder.encode(PacketFactory.createAckPacket(Arrays.copyOfRange(answerBuffer.array(), 2, 4))));
                                out.flush();
                            }
                            else{
                                synchronized (this){
                                    this.notifyAll();
                                };
                            }
                            break;
                        case ACK:
                            short blockNum = (short) (((short) answerBuffer.array()[2]) << 8 | (short) (answerBuffer.array()[3]) & 0x00ff);
                            System.out.println("ACK " + blockNum);
                            if(currentOpcode == DISC && blockNum == 0){
                                keyboardListener.terminate();
                                synchronized (this){
                                    this.notifyAll();
                                }//maybe the keyboard thread is sleeping so we want it to wake up ad stop his run
                                Thread.currentThread().interrupt();
                            }
                            if(!writeQueue.isEmpty() && writeBlock == blockNum){
                                writeBlock++;
                                out.write(encoderDecoder.encode(PacketFactory.createDataPacket(writeQueue.remove(), writeBlock)));
                                out.flush();
                            }
                            else{
                                writeBlock = 0;
                                synchronized (this){
                                    this.notifyAll();
                                }
                            }
                            break;
                        case ERROR:
                            if(currentOpcode == RRQ){
                                String fileToDelete = new String(Arrays.copyOfRange(currentMessage, 2, currentMessage.length - 1));
                                Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "\\" + fileToDelete));
                            }
                            if(currentOpcode == WRQ){
                                writeQueue.clear();
                                writeBlock = 0;
                            }
                            if(currentOpcode == DISC){
                                ;
                            }
                            short errCode = (short) (((short) answerBuffer.array()[2]) << 8 | (short) ( answerBuffer.array()[3]) & 0x00ff);
                            String errorMsg = new String(Arrays.copyOfRange(answerBuffer.array(), 3, answerBuffer.array().length - 1), StandardCharsets.UTF_8);
                            System.out.println("Error " + String.valueOf(errCode) + " " + errorMsg);
                            synchronized (this){
                                this.notifyAll();
                            };
                            break;
                        case BCAST:
                            String fileName = new String(Arrays.copyOfRange(answerBuffer.array(), 3, answerBuffer.array().length - 1), StandardCharsets.UTF_8);
                            System.out.println("BCAST " + (answerBuffer.array()[2] == 0?" del ":" add ") + fileName);
                            synchronized (this){
                                this.notifyAll();
                            };
                            break;
                    }
                    answerBuffer.clear();
                }
                if(!transferedData.isEmpty()){
                    if(currentOpcode == RRQ){
                        String fileName = new String(Arrays.copyOfRange(currentMessage, 2, currentMessage.length - 1), StandardCharsets.UTF_8);
                        while(!transferedData.isEmpty()){
                            Files.write(Paths.get(System.getProperty("user.dir") + "\\" + fileName), transferedData.removeFirst().array(), StandardOpenOption.APPEND);
                        }
                    }
                    if(currentOpcode == DIRQ){
                        Queue<String> fileNames = getFileNames(transferedData);
                        while(!fileNames.isEmpty()){
                            System.out.println(fileNames.remove());
                        }
                    }
                    synchronized (this){
                                this.notifyAll();
                    };
                }
            }
        }
        catch (IOException | InterruptedException e) {
            System.out.println(e.getCause());
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

    public void setKeyboardListener(KeyboardListener keyboardListener){
        this.keyboardListener = keyboardListener;
    }
}
