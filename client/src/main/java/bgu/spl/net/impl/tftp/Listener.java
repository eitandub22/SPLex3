package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
    private String currentCommand;

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
                short answerOpcode = (short) (((short) answerBuffer.array()[0]) << 8 | (short) (answerBuffer.array()[1]) & 0x00ff);
                int answerLength = in.read(answerBuffer.array());
                while(answerLength != -1){
                    switch (answerOpcode){
                        case DATA:
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

            }
        }
        catch (IOException | InterruptedException e) {
            System.out.println(e.getCause());
            throw new RuntimeException(e);
        }
    }
}
