package bgu.spl.net.impl.tftp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class test {
    public static void main(String[] args){
        ByteBuffer errPacket = ByteBuffer.allocate(5 + ("errorDescription: description of an error, can be long description".getBytes(StandardCharsets.UTF_8)).length);
        errPacket.put((byte)0);
        errPacket.put((byte) ((short) 5 & 0xff));
        errPacket.put((byte)(0 >> 8));
        errPacket.put((byte) (1 & 0xff));
        errPacket.put("errorDescription: description of an error, can be long description".getBytes(StandardCharsets.UTF_8));
        errPacket.put(new byte[]{0});
        System.out.println(Arrays.toString(errPacket.array()));
    }

}
