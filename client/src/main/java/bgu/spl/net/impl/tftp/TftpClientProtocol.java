package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.packets.PacketFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if(splitedCommand.length == 0) return new byte[]{0};
        if(userCommands.contains(splitedCommand[0])){
            switch (command){
                case LOGRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    return PacketFactory.createLogPacket(splitedCommand[1]);
                case DELRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    return PacketFactory.createDelPacket(splitedCommand[1]);
                case RRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    Path readPath = Paths.get(System.getProperty("user.dir") + "\\" + splitedCommand[1]);
                    if(Files.notExists(readPath)){
                        try {
                            Files.createFile(readPath);
                        } catch (IOException e) {
                            return new byte[]{1};
                        }
                    }
                    return PacketFactory.createRRQPacket(splitedCommand[1]);
                case WRQ:
                    if(splitedCommand.length != 2) return new byte[]{0};
                    Path writePath = Paths.get(System.getProperty("user.dir") + "\\" + splitedCommand[1]);
                    if(Files.notExists(writePath)){
                        return new byte[]{2};
                    }
                    return PacketFactory.createWRQPacket(splitedCommand[1]);
                case DIRQ:
                    return PacketFactory.createDirPacket();
                case DISC:
                    return PacketFactory.createDiscPacket();
            }
        }
        else{
            return new byte[]{0};
        }
        return new byte[]{0};
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
