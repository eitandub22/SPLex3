package bgu.spl.net.impl.tftp.packetReaders;

import java.nio.ByteBuffer;

public class ACKreader extends PacketReader{
    public static final short OPTCODE = 4;

    public ACKreader(){
        this.pBuffer = ByteBuffer.allocate(4);
        this.pBuffer.put((byte) 0);
        this.pBuffer.put((byte) OPTCODE);
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
