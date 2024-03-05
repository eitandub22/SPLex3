package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private boolean terminate = false;
    private final String NOT_DEFINED = "Not defined, see error message (if any).";
    private final String FILE_NOT_FOUND = "File not found – RRQ DELRQ of non-existing file.";
    private final String ACCESS_VIOLATION = "Access violation – File cannot be written, read or deleted.";
    private final String DISK_FULL = "Disk full or allocation exceeded – No room in disk.";
    private final String ILLEGAL_OPERATION = "Illegal TFTP operation – Unknown Opcode.";
    private final String FILE_EXISTS = "File already exists – File name exists on WRQ.";
    private final String NOT_LOGGED_IN = "User not logged in – Any opcode received before Login completes.";
    private final String ALREADY_LOGGED = "User already logged in – Login username already connected.";
    enum OPCODES {
        READ(1), WRITE(2), DATA(3), ACK(4), ERROR(5), DIR(6), LOGIN(7), DELETE(8), CAST(9), DISCONNECT(10);
        private final int value;

        OPCODES(int value) {
            this.value = value;
        }

        public static OPCODES getOpcode(int value) {
            for (OPCODES opcode : OPCODES.values()) {
                if (opcode.getValue() == value) {
                    return opcode;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    private Connections<byte[]> connections;
    private int connectionId;

    private final byte[] endByte = new byte[]{0};

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(byte[] message) {
        short opcode = (short) (((short) message [0]) << 8 | (short) (message [1]));
        OPCODES opcodeEnum = OPCODES.getOpcode(opcode);
        byte[] data = null;
        switch (opcodeEnum){
            case READ:
                data = Arrays.copyOfRange(message, 2, message.length - 2);
                processRead(data);
                break;
            case WRITE:
                data = Arrays.copyOfRange(message, 2, message.length - 2);
                processWrite(data);
                break;
            case DATA:
                processData();
                break;
            case ACK:
                processAck();
                break;
            case ERROR:
                processError();
                break;
            case DIR:
                processDir();
                break;
            case LOGIN:
                data = Arrays.copyOfRange(message, 2, message.length - 2);
                processLogin(data);
                break;
            case DELETE:
                processDelete(data);
                break;
            case CAST:
                processCast(data);
                break;
            case DISCONNECT:
                processDisconnect();
                break;
        }

    }

    private void processDisconnect() {
        terminate = true;
        connections.disconnect(this.connectionId);
    }

    private void processCast(byte[] data) {
        for(Integer id: connections.getIds()){//TODO implement
            connections.send(id, data);
        }
    }

    private void processDelete(byte[] data) {
        String fileName = new String(data, StandardCharsets.UTF_8);
        try {
            Files.deleteIfExists(Paths.get("Flies" + "\\" + fileName));
        }
        catch (NoSuchFileException e) {
            connections.send(this.connectionId, createErrorPacket((short)1, FILE_NOT_FOUND));
        }
        catch (IOException e) {
            connections.send(this.connectionId, createErrorPacket((short)2, ACCESS_VIOLATION));
        }

    }

    private void processLogin(byte[] data) {
        String userName = new String(data, StandardCharsets.UTF_8);
        if(!connections.getLoggedIn().containsKey(this.connectionId)){//TODO implement
            connections.logIn(this.connectionId, userName);//TODO implement
            connections.send(this.connectionId ,createAckPacket((short)0));
        }
        else{
            connections.send(this.connectionId, createErrorPacket((short)7, ALREADY_LOGGED));
        }
    }

    private void processDir() {

    }

    private void processError() {

    }

    private void processAck() {

    }

    private void processData() {

    }

    private void processWrite(byte[] data) {

    }

    private byte[] processRead(byte[] data) {
        return new byte[0];
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }

    private byte[] createErrorPacket(short errCode, String errorDescription){
        byte[] errPacket = new byte[5 + (errorDescription.getBytes(StandardCharsets.UTF_8)).length];
        byte[] msgStart = new byte[4];
        msgStart[0] = (byte)(0);
        msgStart[1] = (byte) ((short)OPCODES.ERROR.getValue() & 0xff);
        msgStart[2] = (byte)(errCode >> 8);
        msgStart[3] = (byte) (errCode & 0xff);
        byte[] errInBytes = errorDescription.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(msgStart, 0, errPacket, 0, msgStart.length);
        System.arraycopy(errInBytes, 0, errPacket, msgStart.length, errInBytes.length);
        System.arraycopy(endByte, 0, errPacket, msgStart.length + errInBytes.length, endByte.length);
        return errPacket;
    }

    private byte[] createAckPacket(short block_number){
        byte[] ackPacket = new byte[4];
        ackPacket[0] = (byte)(0);
        ackPacket[1] = (byte) ((short)OPCODES.ACK.getValue() & 0xff);
        ackPacket[2] = (byte)(block_number >> 8);
        ackPacket[3] = (byte) (block_number & 0xff);
        return ackPacket;
    }
}
