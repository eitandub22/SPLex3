package bgu.spl.net.impl.tftp.packets.packetReaders;

import java.nio.ByteBuffer;

public class DATAreader extends PacketReader{
    public static final short OPTCODE = 3;
    private short size;
    private short pNum;
    private ByteBuffer shortBuffer;

    public DATAreader(){
        this.pBuffer = ByteBuffer.wrap(new byte[]{0,OPTCODE}, 2, 518);
        this.shortBuffer = ByteBuffer.allocate(2);
        this.pNum = -1;
        this.size = -1;
    }

    public byte[] proccesByte(byte b){
        this.pBuffer.put(b);
        if(this.size == -1){
            this.shortBuffer.put(b);
            if(!this.shortBuffer.hasRemaining()){
                this.shortBuffer.flip();
                this.size = this.shortBuffer.getShort();
                this.shortBuffer.clear();
            }
        }
        else if(this.pNum == -1){
            this.shortBuffer.put(b);
            if(!this.shortBuffer.hasRemaining()){
                this.shortBuffer.flip();
                this.pNum = this.shortBuffer.getShort();
                this.shortBuffer.clear();
            }
        }
        else if(this.pBuffer.position() == this.size + 6){
            this.pBuffer.flip();
            byte[] retArr = new byte[this.pBuffer.remaining()];
            this.pBuffer.get(retArr);
            return retArr;
        }

        return null;
    }
}
