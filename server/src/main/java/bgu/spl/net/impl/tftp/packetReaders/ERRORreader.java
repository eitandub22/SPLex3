package bgu.spl.net.impl.tftp.packetReaders;

import java.nio.ByteBuffer;

public class ERRORreader extends PacketReader{
    public static final short OPTCODE = 5;
    private ByteBuffer shortBuffer;
    private short errorCode;

    public ERRORreader(){
        this.pBuffer = ByteBuffer.wrap(new byte[]{0,OPTCODE}, 2, 518);
        this.shortBuffer = ByteBuffer.allocate(2);
        this.errorCode = -1;
    }

    public byte[] proccesByte(byte b){
        if(this.errorCode == -1){
            this.shortBuffer.put(b);
            if(!this.shortBuffer.hasRemaining()){
                this.shortBuffer.flip();
                this.errorCode = this.shortBuffer.getShort();
                this.shortBuffer.clear();
            }
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
