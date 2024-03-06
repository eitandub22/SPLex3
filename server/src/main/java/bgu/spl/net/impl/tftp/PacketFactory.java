package bgu.spl.net.impl.tftp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class PacketFactory {
    private final byte[] endByte = new byte[]{0};
    public byte[] createErrorPacket(short errCode, String errorDescription){
        ByteBuffer errPacket = ByteBuffer.allocate(5 + (errorDescription.getBytes(StandardCharsets.UTF_8)).length);
        errPacket.put((byte)0);
        errPacket.put((byte) ((short) Opcodes.ERROR.getValue() & 0xff));
        errPacket.put((byte)(errCode >> 8));
        errPacket.put((byte) (errCode & 0xff));
        errPacket.put(errorDescription.getBytes(StandardCharsets.UTF_8));
        errPacket.put(endByte);
        return errPacket.array();
    }

    public byte[] createAckPacket(short block_number){
        ByteBuffer ackPacket = ByteBuffer.allocate(4);
        ackPacket.put((byte)(0));
        ackPacket.put((byte) ((short) Opcodes.ACK.getValue() & 0xff));
        ackPacket.put((byte)(block_number >> 8));
        ackPacket.put((byte) (block_number & 0xff));
        return ackPacket.array();
    }

    public byte[] createBcastPacket(String fileName, int status) {
        ByteBuffer bcastPacket = ByteBuffer.allocate(3 + fileName.getBytes().length);
        bcastPacket.put((byte)(0));
        bcastPacket.put((byte) ((short) Opcodes.CAST.getValue() & 0xff));
        bcastPacket.put((byte)((short)status >> 8));
        bcastPacket.put((byte) ((short)status & 0xff));
        bcastPacket.put(fileName.getBytes());
        bcastPacket.put(endByte);
        return bcastPacket.array();
    }

    public byte[] createDataPacket(byte[] data, int blockNumber){
        ByteBuffer dirPacket = ByteBuffer.allocate(6 + data.length);
        ByteBuffer blockNum = ByteBuffer.allocate(2).putInt(blockNumber);
        ByteBuffer packetSize = ByteBuffer.allocate(2).putInt(data.length);
        dirPacket.put((byte)(0));
        dirPacket.put((byte) ((short) Opcodes.DATA.getValue() & 0xff));
        dirPacket.put(packetSize);
        dirPacket.put(blockNum);
        dirPacket.put(data);
        return dirPacket.array();
    }
}
