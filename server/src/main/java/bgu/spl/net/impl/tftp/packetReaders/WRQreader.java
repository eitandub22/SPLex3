package bgu.spl.net.impl.tftp.packetReaders;

import java.nio.ByteBuffer;

public class WRQreader extends PacketReader{
    public static final short OPTCODE = 2;


    public WRQreader(){
        this.pBuffer = ByteBuffer.wrap(new byte[]{0,OPTCODE}, 2, 518);
    }

    public byte[] proccesByte(byte b){
        if(b == 0){
            byte[] retArr = this.pBuffer.array().clone();
            this.pBuffer.flip();
            this.pBuffer.clear();
            return retArr;
        }

        this.pBuffer.put(b);
        return null;
    }
}
