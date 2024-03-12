package bgu.spl.net.impl.tftp.packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketFactory {
    public static byte[] createLogPacket(String fileName) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (7 & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }

    public static byte[] createDelPacket(String fileName) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (8 & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }

    public static byte[] createRRQPacket(String fileName){
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (1 & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }
    public static byte[] createWRQPacket(String fileName){
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (2 & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }
    public static byte[] createDirPacket(){
        ByteBuffer packetBuffer = ByteBuffer.allocate(2);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (6 & 0xff));
        return packetBuffer.array();
    }
    public static byte[] createDiscPacket(){
        ByteBuffer packetBuffer = ByteBuffer.allocate(2);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (10 & 0xff));
        return packetBuffer.array();
    }

    public static byte[] createAckPacket(byte[] dataBlockNum) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(4);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (4 & 0xff));
        packetBuffer.put(dataBlockNum);
        return packetBuffer.array();
    }

    public static byte[] createDataPacket(byte[] data, short blockNum) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(6 + data.length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (3 & 0xff));
        byte [] block_num = new byte []{(byte) (blockNum >> 8) , (byte) (blockNum & 0xff)};
        packetBuffer.put(ByteBuffer.allocate(2).putShort((short) data.length).array());
        packetBuffer.put(block_num);
        packetBuffer.put(data);
        return packetBuffer.array();
    }
}
