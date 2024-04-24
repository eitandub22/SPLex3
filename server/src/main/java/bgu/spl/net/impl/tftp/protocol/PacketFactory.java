package bgu.spl.net.impl.tftp.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketFactory {
    private static final byte[] endByte = new byte[]{0};
    public static byte[] createErrorPacket(short errCode, String errorDescription){
        ByteBuffer errPacket = ByteBuffer.allocate(5 + (errorDescription.getBytes(StandardCharsets.UTF_8)).length);
        errPacket.put((byte)0);
        errPacket.put((byte) ((short) Opcodes.ERROR.getValue() & 0xff));
        errPacket.put((byte)(errCode >> 8));
        errPacket.put((byte) (errCode & 0xff));
        errPacket.put(errorDescription.getBytes(StandardCharsets.UTF_8));
        errPacket.put(endByte);
        return errPacket.array();
    }

    public static byte[] createAckPacket(short block_number){
        ByteBuffer ackPacket = ByteBuffer.allocate(4);
        ackPacket.put((byte)(0));
        ackPacket.put((byte) ((short) Opcodes.ACK.getValue() & 0xff));
        ackPacket.put((byte)(block_number >> 8));
        ackPacket.put((byte) (block_number & 0xff));
        return ackPacket.array();
    }

    public static byte[] createBcastPacket(String fileName, int status) {
        ByteBuffer bcastPacket = ByteBuffer.allocate(4 + fileName.getBytes(StandardCharsets.UTF_8).length);
        bcastPacket.put((byte)(0));
        bcastPacket.put((byte) ((short) Opcodes.CAST.getValue() & 0xff));
        bcastPacket.put((byte) status);
        bcastPacket.put(fileName.getBytes(StandardCharsets.UTF_8));
        bcastPacket.put(endByte);
        return bcastPacket.array();
    }

    public static byte[] createDataPacket(byte[] data, int blockNumber){
        ByteBuffer dataPacket = ByteBuffer.allocate(6 + data.length);
        ByteBuffer blockNum = ByteBuffer.allocate(2).putShort((short) blockNumber);
        ByteBuffer packetSize = ByteBuffer.allocate(2).putShort((short) data.length);
        dataPacket.put((byte)(0));
        dataPacket.put((byte) ((short) Opcodes.DATA.getValue() & 0xff));
        dataPacket.put(packetSize.array());
        dataPacket.put(blockNum.array());
        dataPacket.put(data);
        return dataPacket.array();
    }
}
