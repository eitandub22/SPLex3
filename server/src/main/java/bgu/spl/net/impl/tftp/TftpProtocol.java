package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private boolean terminate = false;
    private ServerCallBacks serverCallBacks;
    private HashMap<Integer, String> errors = new HashMap<Integer, String>();

    private String[] errorMessages = new String[]{"Not defined, see error message (if any).", "File not found – RRQ DELRQ of non-existing file.", "Access violation – File cannot be written, read or deleted.", "Disk full or allocation exceeded – No room in disk.", "Illegal TFTP operation – Unknown Opcode.", "File already exists – File name exists on WRQ.", "User not logged in – Any opcode received before Login completes.", "User already logged in – Login username already connected."};
    /*enum OPCODES {
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
    }*/

    private Connections<byte[]> connections;
    private int connectionId;

    private PacketFactory packetFactory;

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
            case DISCONNECT:
                processDisconnect();
                break;
        }

    }

    private void processDisconnect() {
        terminate = true;
        connections.disconnect(this.connectionId);
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
            Files.deleteIfExists(Paths.get("Flies" + "\\" + fileName));
        }
        catch (NoSuchFileException e) {
            connections.send(this.connectionId, packetFactory.createErrorPacket((short)1, errors.get(1)));
        }
        catch (IOException e) {
            connections.send(this.connectionId, packetFactory.createErrorPacket((short)2, errors.get(2)));
        }

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

    }

    private void processError() {

    }

    private void processAck() {

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
