package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.packets.PacketFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TftpClientProtocol implements MessagingProtocol<byte[]> {
    private final String LOGRQ = "LOGRQ";
    private final String DELRQ = "DELRQ";
    private final String RRQ = "RRQ";
    private final String WRQ = "WRQ";
    private final String DIRQ = "DIRQ";
    private final String DISC = "DISC";
    private final int CAPACITY = 512;
    Set<String> userCommands = new HashSet<>();
    String[] commands = new String[]{LOGRQ, DELRQ, RRQ, WRQ, DIRQ, DISC};
    private boolean terminate = false;
    public TftpClientProtocol(){
        Collections.addAll(this.userCommands, this.commands);
    }
    @Override
    public byte[] process(byte[] msg) {
        String command = new String(msg, StandardCharsets.UTF_8);
        String[] splitedCommand = command.split(" ");
        if(splitedCommand.length == 0) return new byte[]{0};
        if(userCommands.contains(splitedCommand[0])){
            switch (splitedCommand[0]){
                case LOGRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    return PacketFactory.createLogPacket(splitedCommand[1]);
                case DELRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    return PacketFactory.createDelPacket(splitedCommand[1]);
                case RRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    Path readPath = Paths.get(System.getProperty("user.dir") + "\\" + splitedCommand[1]);
                    if(Files.notExists(readPath)){
                        try {
                            Files.createFile(readPath);
                        } catch (IOException e) {
                            return new byte[]{1};
                        }
                    }
                    return PacketFactory.createRRQPacket(splitedCommand[1]);
                case WRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    Path writePath = Paths.get(System.getProperty("user.dir") + "\\" + splitedCommand[1]);
                    if(Files.notExists(writePath)){
                        return new byte[]{2};
                    }
                    return PacketFactory.createWRQPacket(splitedCommand[1]);
                case DIRQ:
                    return PacketFactory.createDirPacket();
                case DISC:
                    return PacketFactory.createDiscPacket();
            }
        }
        else{
            return new byte[]{0};
        }
        return new byte[]{0};
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

    
    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
