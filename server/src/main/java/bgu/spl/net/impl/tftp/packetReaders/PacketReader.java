package bgu.spl.net.impl.tftp.packetReaders;

import java.nio.ByteBuffer;

/**
 * astract class for family of classes ment to decode each packet type
 */
public abstract class PacketReader{

    protected ByteBuffer pBuffer;

    public static PacketReader makePacketReader(short optcode){

        switch (optcode) {
            case RRQreader.OPTCODE:
                return new RRQreader();
            default:
                return null;
        }
    }

    protected PacketReader(){
        this.pBuffer = null;
    }
    /**
     * procces the next byte
     * @param b, the byte to procces
     * @return the full packet if its ready, else null
     */
    abstract public byte[] proccesByte(byte b);
}
