package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private boolean terminate = false;
    private ServerCallBacks serverCallBacks;
    private final HashMap<Integer, String> errors = new HashMap<Integer, String>();
    private final String[] errorMessages = new String[]{"Not defined, see error message (if any).", "File not found – RRQ DELRQ of non-existing file.", "Access violation – File cannot be written, read or deleted.", "Disk full or allocation exceeded – No room in disk.", "Illegal TFTP operation – Unknown Opcode.", "File already exists – File name exists on WRQ.", "User not logged in – Any opcode received before Login completes.", "User already logged in – Login username already connected."};
    private Connections<byte[]> connections;
    private int connectionId;
    private int readAck = 1;
    private int dirAck = 1;
    private int writeAck = 1;
    private final Deque<ByteBuffer> dirQueue = new ConcurrentLinkedDeque<>();
    private PacketFactory packetFactory;
    private final int CAPACITY = 512;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
        this.packetFactory = new PacketFactory();
        initErrors();
    }

    private void initErrors() {
        for(int i = 0; i < errorMessages.length; i++){
            errors.put(i, errorMessages[i]);
        }
    }

    public void startWithCallBack(int connectionId, Connections<byte[]> connections, ServerCallBacks serverCallBacks) {
        start(connectionId, connections);
        this.serverCallBacks = serverCallBacks;
    }

    @Override
    public void process(byte[] message) {
        short opcode = (short) (((short) message [0]) << 8 | (short) (message [1]));
        Opcodes opcodeEnum = Opcodes.getOpcode(opcode);
        if(serverCallBacks.isLoggedIn(this.connectionId)){
            switch (opcodeEnum){
                case READ:
                    message = Arrays.copyOfRange(message, 2, message.length - 2);
                    processRead(message);
                    break;
                case WRITE:
                    message = Arrays.copyOfRange(message, 2, message.length - 2);
                    processWrite(message);
                    break;
                case DATA:
                    processData();
                    break;
                case ACK:
                    processAck(message);
                    break;
                case DIR:
                    processDir();
                    break;
                case LOGIN:
                    message = Arrays.copyOfRange(message, 2, message.length - 2);
                    processLogin(message);
                    break;
                case DELETE:
                    processDelete(message);
                    break;
                case DISCONNECT:
                    processDisconnect();
                    break;
                default:
                    connections.send(this.connectionId, packetFactory.createErrorPacket((short) 4, errors.get(4)));
            }
        }
        else{
            connections.send(this.connectionId, packetFactory.createErrorPacket((short) 6, errors.get(6)));
        }
    }

    private void processDisconnect() {
        if(serverCallBacks.logout(this.connectionId)){
            terminate = true;
            connections.send(this.connectionId, packetFactory.createAckPacket((short)0));
            connections.disconnect(this.connectionId);//closing my end of the socket
        }
        else{
            connections.send(this.connectionId, packetFactory.createErrorPacket((short) 6, errors.get(6)));
        }
    }

    private void sendCast(String fileName, int status) {
        byte[] castPacket = packetFactory.createBcastPacket(fileName, status);
        for(Map.Entry<String, Integer> user: serverCallBacks.getLoggedIn().entrySet()){
            connections.send(user.getValue(), castPacket);
        }
    }

    private void processDelete(byte[] data) {
        String fileName = new String(data, StandardCharsets.UTF_8);
        try {
            Files.deleteIfExists(Paths.get("Files" + "\\" + fileName));
        }
        catch (NoSuchFileException e) {
            connections.send(this.connectionId, packetFactory.createErrorPacket((short)1, errors.get(1)));
            return;
        }
        catch (IOException e) {
            connections.send(this.connectionId, packetFactory.createErrorPacket((short)2, errors.get(2)));
            return;
        }
        connections.send(this.connectionId, packetFactory.createAckPacket((short)0));
    }

    private void processLogin(byte[] data) {
        String userName = new String(data, StandardCharsets.UTF_8);
        if(serverCallBacks.login(this.connectionId, userName)){
            connections.send(this.connectionId ,packetFactory.createAckPacket((short)0));
        }
        else{
            connections.send(this.connectionId, packetFactory.createErrorPacket((short)7, errors.get(7)));
        }
    }

    private void processDir() {
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
        byte[] dirPacket = packetFactory.createDataPacket(currentData.array(), dirAck);
        dirAck++;
        connections.send(this.connectionId, dirPacket);
    }
    private void processAck(byte[] data) {
        byte[] blockArr = new byte[2];
        blockArr = Arrays.copyOfRange(data, 2, data.length - 1);
        short blockNum = (short) ((( short ) blockArr [0]) << 8 | ( short ) ( blockArr [1]) );
        if(blockNum == writeAck){
            //continue writing to client
            writeAck++;
        }
        else if(blockNum == dirAck){
            processDir();
        }
        else{
            connections.send(this.connectionId, packetFactory.createErrorPacket((short) 0, errors.get(0)));
        }
    }

    private void processData() {

    }

    private void processWrite(byte[] data) {

    }

    private void processRead(byte[] data) {

    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }


}
