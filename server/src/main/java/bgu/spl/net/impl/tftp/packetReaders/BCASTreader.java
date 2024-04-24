package bgu.spl.net.impl.tftp.packetReaders;

import java.nio.ByteBuffer;

public class BCASTreader extends PacketReader{
    public static final short OPTCODE = 9;

    public BCASTreader(){
        this.pBuffer = ByteBuffer.allocate(518);
        this.pBuffer.put((byte) 0);
        this.pBuffer.put((byte) OPTCODE);
    }

    public byte[] proccesByte(byte b){
        if(this.pBuffer.position() == 2){
            this.pBuffer.put(b);
            return null;
        }
        else if(b == 0){
            this.pBuffer.flip();
            byte[] retArr = new byte[this.pBuffer.remaining()];
            this.pBuffer.get(retArr);
            return retArr;
        }
        this.pBuffer.put(b);
        return null;
    }
}
