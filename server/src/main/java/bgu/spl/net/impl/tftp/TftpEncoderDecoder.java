package bgu.spl.net.impl.tftp;


import java.nio.ByteBuffer;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.tftp.packetReaders.DIRQreader;
import bgu.spl.net.impl.tftp.packetReaders.DISCreader;
import bgu.spl.net.impl.tftp.packetReaders.PacketReader;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private final ByteBuffer optcodeBuffer = ByteBuffer.allocate(2);
    private PacketReader pReader = null;
    private short optcode = -1;
    @Override
    public byte[] decodeNextByte(byte nextByte) {

        if (optcode == -1) { //indicates that we are still reading the optcode
            optcodeBuffer.put(nextByte);
            if (!optcodeBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the length
                optcodeBuffer.flip();
                optcode = optcodeBuffer.getShort();
                optcodeBuffer.clear();
<<<<<<< HEAD
                if(optcode == DIRQreader.OPTCODE || optcode == DIRQreader.OPTCODE)
                {
                    byte[] retArr = new byte[]{(byte) (optcode >> 8), (byte) (optcode & 0xff)};
                    this.optcode = -1;
                    return retArr;
=======
                if(optcode == DIRQreader.OPTCODE || optcode == DISCreader.OPTCODE) {
                    byte[] res = new byte[]{(byte) (optcode >> 8), (byte) (optcode & 0xff)};
                    optcode = - 1;
                    return res;//DIR or DISCONNECT
>>>>>>> 0ee8a28569c00233c49867ba47bc70d4bd7fd1ab
                }
            }
        } else {
            if(pReader == null) pReader = PacketReader.makePacketReader(optcode);
            byte[] retArr = pReader.proccesByte(nextByte);
            if(retArr != null){
                pReader = null;
                optcode = -1;
            }
            
            return retArr;
        }

        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }
}