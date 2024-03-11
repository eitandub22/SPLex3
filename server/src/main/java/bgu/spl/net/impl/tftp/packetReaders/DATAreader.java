package bgu.spl.net.impl.tftp.packetReaders;

import java.nio.ByteBuffer;

public class DATAreader extends PacketReader{
    public static final short OPTCODE = 3;
    private static shortBuffer;

    public DATAreader(){
        this.pBuffer = ByteBuffer.wrap(new byte[]{0,OPTCODE}, 2, 518);
        private static shortBuffer
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
