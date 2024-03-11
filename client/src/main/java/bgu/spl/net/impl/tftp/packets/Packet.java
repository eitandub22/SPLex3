package bgu.spl.net.impl.tftp.packets;

public interface Packet {
    public byte[] createPacket(String message);
}
