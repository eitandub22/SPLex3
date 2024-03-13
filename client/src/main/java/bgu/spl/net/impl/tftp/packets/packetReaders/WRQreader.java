package bgu.spl.net.impl.tftp.packets.packetReaders;

import java.nio.ByteBuffer;

public class WRQreader extends PacketReader{
    public static final short OPTCODE = 2;


    public WRQreader(){
        this.pBuffer = ByteBuffer.allocate(518);
        this.pBuffer.put((byte) 0);
        this.pBuffer.put((byte) OPTCODE);
    }

    public byte[] proccesByte(byte b){
        if(b == 0){
            this.pBuffer.flip();
            byte[] retArr = new byte[this.pBuffer.remaining()];
            this.pBuffer.get(retArr);
            return retArr;
        }

        this.pBuffer.put(b);
        return null;
    }
}
