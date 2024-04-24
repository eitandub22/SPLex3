package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.packets.PacketFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TftpServerProtocol implements MessagingProtocol<byte[]> {
    private final String LOGRQ = "LOGRQ";
    private final String DELRQ = "DELRQ";
    private final String RRQ = "RRQ";
    private final String WRQ = "WRQ";
    private final String DIRQ = "DIRQ";
    private final String DISC = "DISC";
    Set<String> userCommands = new HashSet<>();
    private int readAck = -1;
    private int dirAck = -1;
    private int writeAck = -1;
    private final LinkedList<ByteBuffer> dirQueue = new LinkedList<>();
    private final Deque<ByteBuffer> readQueue = new ConcurrentLinkedDeque<>();
    String[] commands = new String[]{LOGRQ, DELRQ, RRQ, WRQ, DIRQ, DISC};
    private boolean terminate = false;
    private short opcode;
    private String readingFile;

    public TftpServerProtocol(){
        Collections.addAll(this.userCommands, this.commands);
    }
    public void setCurrentCommand(short opcode){
        this.opcode = opcode;
    }
    public void setReadingFile(String readingFile){
        this.readingFile = readingFile;
    }
    @Override
    public byte[] process(byte[] message) {
        short opcode = (short) (((short) message [0]) << 8 | (short) (message [1]) & 0x00ff);
        System.out.println(opcode);
        Opcodes opcodeEnum = Opcodes.getOpcode(opcode);
        switch (opcodeEnum){
            case DATA:
                //message = Arrays.copyOfRange(message, 2, message.length);
                //processData(message, this.currentWriteFileName);
                break;
            case ACK:
                try{
                    processAck(message);
                }
                catch (IOException e){
                    ;
                }
                break;
            case ERROR:
                short errCode = (short) (((short) message[2]) << 8 | (short) ( message[3]) & 0x00ff);
                String errorMsg = new String(Arrays.copyOfRange(message, 3, message.length - 1), StandardCharsets.UTF_8);
                System.out.println("Error " + String.valueOf(errCode) + " " + errorMsg);
                return null;
            case CAST:
                String fileName = new String(Arrays.copyOfRange(message, 3, message.length - 1), StandardCharsets.UTF_8);
                System.out.println("BCAST " + (message[2] == 0?" del ":" add ") + fileName);
                return null;
        }
        return new byte[0];
    }

    private void processAck(byte[] data) throws IOException {
        System.out.println(this.opcode);
        byte[] blockArr = Arrays.copyOfRange(data, 2, data.length);
        short blockNum = (short) (((short) blockArr[0]) << 8 | (short) (blockArr[1]) & 0x00ff);
        if(this.opcode == Opcodes.LOGIN.getValue() || this.opcode == Opcodes.CAST.getValue()){
            System.out.println("ACK " + blockNum);
        }

        if(readAck > -1 && blockNum == readAck){
            readQueue.add(ByteBuffer.wrap(data));
            readAck++;
            if(data.length < 512){
                while(!readQueue.isEmpty()){
                    Files.write(Paths.get(System.getProperty("user.dir") + "\\" + this.readingFile), readQueue.remove().array(), StandardOpenOption.APPEND);
                }
                readAck = -1;
            }
        }
        else if(dirAck > -1 && blockNum == dirAck){
           dirQueue.add(ByteBuffer.wrap(data));
           dirAck++;
            if(data.length < 512){
                Queue<String> fileNames = getFileNames(this.dirQueue);
                while(!fileNames.isEmpty()){
                    System.out.println(fileNames.remove());
                }
                dirAck = -1;
            }
        }
    }

    /*private void processData(byte[] data, String writeFileName) {
        short packetSize = (short) (((short) data[0]) << 8 | (short) (data[1]) & 0x00ff);
        writeAck = (short) (((short) data[2]) << 8 | (short) (data[3]) & 0x00ff);//the current block number
        if(TransferHandler.handleData(data, writeFileName)){
            connections.send(this.connectionId, (PacketFactory.createAckPacket((short) writeAck)));
        }
        else{
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 2, errors.get(2))));
            return;
        }
        if(packetSize < CAPACITY){
            sendCast(writeFileName, 1);
        }
    }*/

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
