package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.protocol.TftpProtocol;
import bgu.spl.net.srv.*;


import java.util.concurrent.ConcurrentHashMap;

public class TftpServer{
    private static ConcurrentHashMap<String, Integer> loggedUsers = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> uploadingFiles = new ConcurrentHashMap<>();
    public static void main(String[] args){
        Server.threadPerClient(
                7777, //port
                () -> new TftpProtocol(loggedUsers, uploadingFiles), //protocol factory
                TftpEncoderDecoder::new //encoder decoder factory
        ,new ConnectionsImpl<byte[]>()).serve();
    }

}

