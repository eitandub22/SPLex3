package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;

public class PacketFactory {
    private final byte[] endByte = new byte[]{0};
    public byte[] createErrorPacket(short errCode, String errorDescription){
        byte[] errPacket = new byte[5 + (errorDescription.getBytes(StandardCharsets.UTF_8)).length];
        byte[] msgStart = new byte[4];
        msgStart[0] = (byte)(0);
        msgStart[1] = (byte) ((short) Opcodes.ERROR.getValue() & 0xff);
        msgStart[2] = (byte)(errCode >> 8);
        msgStart[3] = (byte) (errCode & 0xff);
        byte[] errInBytes = errorDescription.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(msgStart, 0, errPacket, 0, msgStart.length);
        System.arraycopy(errInBytes, 0, errPacket, msgStart.length, errInBytes.length);
        System.arraycopy(endByte, 0, errPacket, msgStart.length + errInBytes.length, endByte.length);
        return errPacket;
    }

    public byte[] createAckPacket(short block_number){
        byte[] ackPacket = new byte[4];
        ackPacket[0] = (byte)(0);
        ackPacket[1] = (byte) ((short) Opcodes.ACK.getValue() & 0xff);
        ackPacket[2] = (byte)(block_number >> 8);
        ackPacket[3] = (byte) (block_number & 0xff);
        return ackPacket;
    }

    public byte[] createBcastPacket(String fileName, int status) {
        byte[] bcastPacket = new byte[3 + fileName.getBytes().length];
        byte[] msgStart = new byte[4];
        msgStart[0] = (byte)(0);
        msgStart[1] = (byte) ((short) Opcodes.CAST.getValue() & 0xff);
        msgStart[2] = (byte)((short)status >> 8);
        msgStart[3] = (byte) ((short)status & 0xff);
        System.arraycopy(msgStart, 0, bcastPacket, 0, msgStart.length);
        System.arraycopy(fileName.getBytes(), 0, bcastPacket, msgStart.length, fileName.getBytes().length);
        System.arraycopy(endByte, 0, bcastPacket, msgStart.length + fileName.getBytes().length, endByte.length);
        return bcastPacket;
    }
}
