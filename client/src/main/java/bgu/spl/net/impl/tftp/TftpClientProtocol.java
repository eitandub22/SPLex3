package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.packets.PacketFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TftpClientProtocol implements MessagingProtocol<byte[]> {
    private final String LOGRQ = "LOGRQ";
    private final String DELRQ = "DELRQ";
    private final String RRQ = "RRQ";
    private final String WRQ = "WRQ";
    private final String DIRQ = "DIRQ";
    private final String DISC = "DISC";
    Set<String> userCommands = new HashSet<>();
    String[] commands = new String[]{LOGRQ, DELRQ, RRQ, WRQ, DIRQ, DISC};
    private boolean terminate = false;
    public TftpClientProtocol(){
        Collections.addAll(this.userCommands, this.commands);
    }
    @Override
    public byte[] process(byte[] msg) {
        String command = new String(msg, StandardCharsets.UTF_8);
        String[] splitedCommand = command.split(" ");
        if(splitedCommand.length == 0) return null;//or print an error
        if(userCommands.contains(splitedCommand[0])){
            switch (command){
                case LOGRQ:
                    if(splitedCommand.length != 2) return null;//or error
                    return PacketFactory.createLogPacket(splitedCommand[1]);
                case DELRQ:
                    if(splitedCommand.length != 2) return null;//or error
                    return PacketFactory.createDelPacket(splitedCommand[1]);
                case RRQ:
                    if(splitedCommand.length != 2) return null;//or error
                    return PacketFactory.createRRQPacket(splitedCommand[1]);
                case WRQ:
                    if(splitedCommand.length != 2) return null;//or error
                    return PacketFactory.createWRQPacket(splitedCommand[1]);
                case DIRQ:
                    return PacketFactory.createDirPacket();
                case DISC:
                    return PacketFactory.createDiscPacket();
            }
        }
        else{
            synchronized (System.out){
                System.out.println("The command you enter is illegal");
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
