package bgu.spl.net.impl.tftp.packets.packetReaders;

import java.nio.ByteBuffer;

public class RRQreader extends PacketReader{
    public static final short OPTCODE = 1;


    public RRQreader(){
        this.pBuffer = ByteBuffer.wrap(new byte[]{0,OPTCODE}, 2, 518);
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
