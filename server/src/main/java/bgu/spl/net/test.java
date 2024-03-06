package bgu.spl.net;

import java.nio.ByteBuffer;

public class test {
    public static void main(String[] args){
        ByteBuffer currentFile = ByteBuffer.wrap(new byte[]{1,2,3,4});
        ByteBuffer currentData = ByteBuffer.allocate(5);
        currentData.put(currentFile.array(), 0, currentFile.capacity());
        System.out.println(currentData.position());
    }
}
