package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.impl.tftp.protocol.TftpProtocol;
import bgu.spl.net.srv.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TftpServer{
    private static ConcurrentHashMap<String, Integer> loggedUsers;
    private static ConcurrentHashMap<String, Boolean> uploadingFiles;
    public static void main(String[] args){
        Server.threadPerClient(
                7777, //port
                () -> new TftpProtocol(loggedUsers, uploadingFiles), //protocol factory
                TftpEncoderDecoder::new //encoder decoder factory
        ,new ConnectionsImpl<byte[]>()).serve();
    }

}

