package bgu.spl.net.impl.tftp.packetReaders;

public class DISCreader extends PacketReader{
    public static final short OPTCODE = 10;
    public byte[] proccesByte(byte b){
        return new byte[]{(byte) (OPTCODE >> 8), (byte) (OPTCODE & 0xff)};
    }
}
