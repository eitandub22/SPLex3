package bgu.spl.net.impl.tftp;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private final ByteBuffer optcodeBuffer = ByteBuffer.allocate(2);
    private short optcode = -1;
    private LinkedList<Byte> currPacket = new LinkedList<Byte>();
    private int packetBytesIndex = 0;
    private byte[] returnMsg = null;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        //TODO implement
        return new byte[0];
    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }
}