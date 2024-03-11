package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    @Override
    public byte[] decodeNextByte(byte nextByte) {
        return new byte[0];
    }

    @Override
    public byte[] encode(byte[] message) {
        return new byte[0];
    }
}
