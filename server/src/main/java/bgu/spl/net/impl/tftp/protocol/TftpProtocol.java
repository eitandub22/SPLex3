package bgu.spl.net.impl.tftp.protocol;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private boolean terminate = false;
    private final HashMap<Integer, String> errors = new HashMap<Integer, String>();
    private final String[] errorMessages = new String[]{"Not defined, see error message (if any).", "File not found - RRQ DELRQ of non-existing file.", "Access violation - File cannot be written, read or deleted.", "Disk full or allocation exceeded - No room in disk.", "Illegal TFTP operation - Unknown Opcode.", "File already exists - File name exists on WRQ.", "User not logged in - Any opcode received before Login completes.", "User already logged in - Login username already connected."};    private Connections<byte[]> connections;
    private int connectionId;
    private int readAck = -1;
    private int dirAck = -1;
    private int writeAck = -1;
    private final Deque<ByteBuffer> dirQueue = new ConcurrentLinkedDeque<>();
    private final Deque<ByteBuffer> readQueue = new ConcurrentLinkedDeque<>();
    private ConcurrentHashMap<String, Integer> loggedUsers;
    private boolean isLogged = false;
    private String currentReadFileName;
    private String currentWriteFileName;
    private ConcurrentHashMap<String, Boolean> uploadingFiles;
    private final int CAPACITY = 512;
    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    private void initErrors() {
        for(int i = 0; i < errorMessages.length; i++){
            errors.put(i, errorMessages[i]);
        }
    }

    public TftpProtocol(ConcurrentHashMap<String, Integer> loggedUsers, ConcurrentHashMap<String, Boolean> uploadingFiles){
        this.loggedUsers = loggedUsers;
        initErrors();
        this.uploadingFiles = uploadingFiles;
    }

    @Override
    public void process(byte[] message) {
        short opcode = (short) (((short) message [0]) << 8 | (short) (message [1]) & 0x00ff);
        System.out.println(opcode);
        Opcodes opcodeEnum = Opcodes.getOpcode(opcode);
        if(isLogged){
            switch (opcodeEnum){
                case READ:
                    message = Arrays.copyOfRange(message, 2, message.length);
                    processRead(message);
                    break;
                case WRITE:
                    message = Arrays.copyOfRange(message, 2, message.length);
                    processWrite(message);
                    break;
                case DATA:
                    message = Arrays.copyOfRange(message, 2, message.length);
                    processData(message, this.currentWriteFileName);
                    break;
                case ACK:
                    processAck(message);
                    break;
                case DIR:
                    processDir();
                    break;
                case DELETE:
                    processDelete(message);
                    break;
                case LOGIN:
                    connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 7, errors.get(7))));
                    break;
                case DISCONNECT:
                    processDisconnect();
                    break;
                default:
                    connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 4, errors.get(4))));
            }
        }
        else{
            if(opcodeEnum == Opcodes.LOGIN){
                message = Arrays.copyOfRange(message, 2, message.length - 1);
                processLogin(message);
            }
            else if(!opcodeEnum.exists(opcodeEnum.getValue())){
                connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 4, errors.get(4))));
            }
            else{
                connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 6, errors.get(6))));
            }
        }
    }

    private void processDisconnect() {
        if(logout()){
            terminate = true;
            isLogged = false;
            connections.send(this.connectionId, (PacketFactory.createAckPacket((short)0)));
            connections.disconnect(this.connectionId);//closing my end of the socket
        }
        else{
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 6, errors.get(6))));
        }
    }

    private boolean logout() {
        for(Map.Entry<String, Integer> user : loggedUsers.entrySet()){
            if(user.getValue() == connectionId){
                loggedUsers.remove(user.getKey());
                return true;
            }
        }
        return false;
    }

    private void sendCast(String fileName, int status) {
        byte[] castPacket = PacketFactory.createBcastPacket(fileName, status);
        for(Map.Entry<String, Integer> user: loggedUsers.entrySet()){
            connections.send(user.getValue(), (castPacket));
        }
    }

    private void processDelete(byte[] data) {
        String fileName = new String(Arrays.copyOfRange(data, 2, data.length), StandardCharsets.UTF_8);
        try {
            Files.deleteIfExists(Paths.get("Files/" + fileName));
        }
        catch (NoSuchFileException e) {
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short)1, errors.get(1))));
            return;
        }
        catch (IOException e) {
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short)2, errors.get(2))));
            return;
        }
        connections.send(this.connectionId, (PacketFactory.createAckPacket((short)0)));
        sendCast(fileName, 0);
    }

    private void processLogin(byte[] data) {
        String userName = new String(data, StandardCharsets.UTF_8);
        if(login(userName)){
            connections.send(this.connectionId , (PacketFactory.createAckPacket((short)0)));
            isLogged = true;
        }
        else{
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short)7, errors.get(7))));
        }
    }

    private boolean login(String userName) {
        if(loggedUsers.containsKey(userName)) return false;
        else{
            loggedUsers.put(userName, connectionId);
            isLogged = true;
            return true;
        }
    }

    private void processDir() {
        dirAck = 1;
        TransferHandler.startDir(this.dirQueue, uploadingFiles);
        continueDir();
    }

    private void continueDir(){
        byte[] currentData = TransferHandler.handleDir(this.dirQueue);
        byte[] dirPacket = PacketFactory.createDataPacket(currentData, dirAck);
        connections.send(this.connectionId, dirPacket);
    }

    private void processAck(byte[] data) {
        byte[] blockArr = Arrays.copyOfRange(data, 2, data.length);
        short blockNum = (short) (((short) blockArr[0]) << 8 | (short) (blockArr[1]) & 0x00ff);
        if(readAck > -1 && blockNum == readAck){
            if(!readQueue.isEmpty()){
                readAck++;
                continueRead();
            }
            else{
                readAck = -1;
            }
        }
        else if(dirAck > -1 && blockNum == dirAck){
            if(!dirQueue.isEmpty()) {
                dirAck++;
                continueDir();
            }
            else{
                dirAck = -1;
            }
        }
        else{
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 0, errors.get(0))));
        }
    }

    private void processData(byte[] data, String writeFileName) {
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
    }

    private void processWrite(byte[] data) {
        String fileName = new String(data, StandardCharsets.UTF_8);
        if(Files.exists(Paths.get("Files/" + fileName))){
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 5, errors.get(5))));
            return;
        }
        try{
            Files.createFile(Paths.get("Files/" + fileName));
        }
        catch (IOException exception){
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 2, errors.get(2))));
            return;
        }
        this.currentWriteFileName = fileName;
        uploadingFiles.put(fileName, true);
        writeAck = 0;
        connections.send(this.connectionId, (PacketFactory.createAckPacket((short) writeAck)));
    }

    private void processRead(byte [] data) {
        readAck = 1;
        String fileName = new String(data,  StandardCharsets.UTF_8);
        this.currentReadFileName = fileName;
        if(Files.exists(Paths.get("Files/" + fileName))){
            TransferHandler.startRead(this.readQueue, this.currentReadFileName);
            continueRead();
        }
        else{
            connections.send(this.connectionId, (PacketFactory.createErrorPacket((short) 1, errors.get(1))));
        }
    }

    private void continueRead(){
        byte[] currentData = TransferHandler.handleRead(this.readQueue);
        connections.send(this.connectionId, PacketFactory.createDataPacket(currentData, readAck));
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }

}
