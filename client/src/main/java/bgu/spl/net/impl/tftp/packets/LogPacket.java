package bgu.spl.net.impl.tftp.packets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LogPacket implements Packet{
    @Override
    public byte[] createPacket(String fileName) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + fileName.getBytes(StandardCharsets.UTF_8).length);
        packetBuffer.put((byte) 0);
        packetBuffer.put((byte) (1 & 0xff));
        packetBuffer.put(fileName.getBytes(StandardCharsets.UTF_8));
        packetBuffer.put((byte) 0);
        return packetBuffer.array();
    }
}
