package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Listener implements Runnable{
    private BlockingQueue<byte[]> messageQueue;
    private String host;
    private MessageEncoderDecoder<byte[]> encoderDecoder;
    private final int CAPACITY = 512;
    private final int DATA = 3;
    private final int ACK = 4;
    private final int ERROR = 5;
    private final int BCAST = 9;

    public Listener(BlockingQueue<byte[]> messageQueue, String host, MessageEncoderDecoder<byte[]> encoderDecoder){
        this.messageQueue = messageQueue;
        this.host = host;
        this.encoderDecoder = encoderDecoder;
    }
    @Override
    public void run() {
        try (Socket sock = new Socket(host, 7777);
             BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream())) {
            while(!Thread.currentThread().isInterrupted()){
                byte[] currentMessage = messageQueue.take();
                short currentOpcode = (short) (((short) currentMessage[0]) << 8 | (short) (currentMessage[1]) & 0x00ff);
                if (currentMessage.length == 1) {
                    switch (currentMessage[0]) {
                        case 0://invalid input error
                            System.out.println("Input is invalid");
                            continue;
                        case 1://file exists error
                            System.out.println("file already exists");
                            continue;
                        case 2://file not exists error
                            System.out.println("file does not exists");
                            continue;
                    }
                }
                out.write(encoderDecoder.encode(currentMessage));
                out.flush();

                ByteBuffer answerBuffer = ByteBuffer.allocate(CAPACITY);
                LinkedList<ByteBuffer> transferedData = new LinkedList<>();
                short answerOpcode = (short) (((short) answerBuffer.array()[0]) << 8 | (short) (answerBuffer.array()[1]) & 0x00ff);
                int answerLength = in.read(answerBuffer.array());
                while(answerLength != -1){
                    switch (answerOpcode){
                        case DATA:
                            if(answerLength > 0 && answerLength <= CAPACITY){
                                transferedData.add(ByteBuffer.wrap(answerBuffer.array()));
                            }
                            break;
                        case ACK:
                            short blockNum = (short) (((short) answerBuffer.array()[2]) << 8 | (short) (answerBuffer.array()[3]) & 0x00ff);
                            System.out.println("ACK " + blockNum);
                            break;
                        case ERROR:
                            if(currentOpcode == 1){//RRQ
                                String fileToDelete = new String(Arrays.copyOfRange(currentMessage, 2, currentMessage.length - 1));
                                Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "\\" + fileToDelete));
                            }
                            if(currentOpcode == 2){//WRQ
                                //stop the transfer
                            }
                            if(currentOpcode == 10){//DISC
                                //dont exist the program
                            }
                            short errCode = (short) (((short) answerBuffer.array()[2]) << 8 | (short) ( answerBuffer.array()[3]) & 0x00ff);
                            String errorMsg = new String(Arrays.copyOfRange(answerBuffer.array(), 3, answerBuffer.array().length - 1), StandardCharsets.UTF_8);
                            System.out.println("Error " + String.valueOf(errCode) + " " + errorMsg);
                            break;
                        case BCAST:
                            String fileName = new String(Arrays.copyOfRange(answerBuffer.array(), 3, answerBuffer.array().length - 1), StandardCharsets.UTF_8);
                            System.out.println("BCAST " + (answerBuffer.array()[2] == 0?" del ":" add ") + fileName);
                            break;
                    }
                    answerBuffer.clear();
                    answerLength = in.read(answerBuffer.array());
                }
                if(!transferedData.isEmpty()){
                    if(currentOpcode == 1){//RRQ
                        String fileName = new String(Arrays.copyOfRange(currentMessage, 2, currentMessage.length - 1), StandardCharsets.UTF_8);
                        while(!transferedData.isEmpty()){
                            Files.write(Paths.get(System.getProperty("user.dir") + "\\" + fileName), transferedData.getFirst().array(), StandardOpenOption.APPEND);
                        }
                    }
                    if(currentOpcode == 6){//DIRQ
                        Queue<String> fileNames = getFileNames(transferedData);
                        while(!fileNames.isEmpty()){
                            System.out.println(fileNames.remove());
                        }
                    }
                }
            }
        }
        catch (IOException | InterruptedException e) {
            System.out.println(e.getCause());
            throw new RuntimeException(e);
        }
    }

    private Queue<String> getFileNames(LinkedList<ByteBuffer> list) {
        Queue<String> fileNames = new LinkedList<>();
        ArrayList<Byte> fileName = new ArrayList<>();
        for (ByteBuffer buffer : list) {
            for(int i = 0; i < buffer.capacity(); i++){
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
}
