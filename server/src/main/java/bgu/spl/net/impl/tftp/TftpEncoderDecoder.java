package bgu.spl.net.impl.tftp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private final ByteBuffer optcodeBuffer = ByteBuffer.allocate(2);
    private short optcode = -1;
    private int objectBytesIndex = 0;


    @Override
    public byte[] decodeNextByte(byte nextByte) {
        //TODO implement

        if (optcode == -1) { //indicates that we are still reading the optcode
            optcodeBuffer.put(nextByte);
            if (!optcodeBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the length
                optcodeBuffer.flip();
                optcode = optcodeBuffer.getShort();
                objectBytesIndex = 0;
                optcodeBuffer.clear();
            }
        } else {
            optcode = -1;
            return null;
        }
        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }
}