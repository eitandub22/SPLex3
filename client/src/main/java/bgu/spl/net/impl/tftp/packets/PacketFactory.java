package bgu.spl.net.impl.tftp.packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import bgu.spl.net.impl.tftp.packets.packetReaders.ACKreader;
import bgu.spl.net.impl.tftp.packets.packetReaders.DATAreader;
import bgu.spl.net.impl.tftp.packets.packetReaders.DELRQreader;
import bgu.spl.net.impl.tftp.packets.packetReaders.DIRQreader;
import bgu.spl.net.impl.tftp.packets.packetReaders.DISCreader;
import bgu.spl.net.impl.tftp.packets.packetReaders.LOGRQreader;
import bgu.spl.net.impl.tftp.packets.packetReaders.RRQreader;
import bgu.spl.net.impl.tftp.packets.packetReaders.WRQreader;

public class PacketFactory {
    public static byte[] createLogPacket(String fileName) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (LOGRQreader.OPTCODE & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }

    public static byte[] createDelPacket(String fileName) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (DELRQreader.OPTCODE & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }

    public static byte[] createRRQPacket(String fileName){
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (RRQreader.OPTCODE & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }
    public static byte[] createWRQPacket(String fileName){
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (WRQreader.OPTCODE & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }
    public static byte[] createDirPacket(){
        ByteBuffer packetBuffer = ByteBuffer.allocate(2);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (DIRQreader.OPTCODE & 0xff));
        return packetBuffer.array();
    }
    public static byte[] createDiscPacket(){
        ByteBuffer packetBuffer = ByteBuffer.allocate(2);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (DISCreader.OPTCODE & 0xff));
        return packetBuffer.array();
    }

    public static byte[] createAckPacket(byte[] dataBlockNum) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(4);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (ACKreader.OPTCODE & 0xff));
        packetBuffer.put(dataBlockNum);
        return packetBuffer.array();
    }

    public static byte[] createDataPacket(byte[] data, short blockNum) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(6 + data.length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (DATAreader.OPTCODE & 0xff));
        byte [] block_num = new byte []{(byte) (blockNum >> 8) , (byte) (blockNum & 0xff)};
        packetBuffer.put(ByteBuffer.allocate(2).putShort((short) data.length).array());
        packetBuffer.put(block_num);
        packetBuffer.put(data);
        return packetBuffer.array();
    }
}
