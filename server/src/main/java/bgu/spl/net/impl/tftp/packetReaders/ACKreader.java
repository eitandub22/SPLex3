package bgu.spl.net.impl.tftp.packetReaders;

import java.nio.ByteBuffer;

public class ACKreader extends PacketReader{
    public static final short OPTCODE = 4;

    public ACKreader(){
        this.pBuffer = ByteBuffer.wrap(new byte[]{0,OPTCODE}, 2, 4);
    }

    public byte[] proccesByte(byte b){
        this.pBuffer.put(b);
        if(!this.pBuffer.hasRemaining()){
            this.pBuffer.flip();
            byte[] retArr = new byte[this.pBuffer.remaining()];
            this.pBuffer.get(retArr);
            return retArr;
        }
        return null;
    }
}
