package bgu.spl.net.impl.tftp.packetReaders;

public class DIRQreader extends PacketReader{
    public static final short OPTCODE = 6;
    public byte[] proccesByte(byte b){
        return new byte[]{(byte) (OPTCODE >> 8), (byte) (OPTCODE & 0xff)};
    }
}
