package bgu.spl.net.impl.tftp;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.tftp.packets.PacketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.BlockingQueue;
public class Listener implements Runnable{
    private MessageEncoderDecoder<byte[]> encoderDecoder;
    TftpClientProtocol protocol;
    private KeyboardListener keyboardListener;
    private BufferedInputStream in;
    BufferedOutputStream out;
    public Listener(MessageEncoderDecoder<byte[]> encoderDecoder, BufferedInputStream in, BufferedOutputStream out, TftpClientProtocol protocol){
        this.encoderDecoder = encoderDecoder;
        this.in = in;
        this.out = out;
        this.protocol = protocol;
    }
    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){

            byte[] ansArr = null;
            ByteBuffer answerBuffer = null;
            try{
            while((ansArr = encoderDecoder.decodeNextByte((byte)in.read())) == null){}
            }catch(IOException e){}
            this.protocol.process(ansArr);
        }
    }
    
    public void setKeyboardListener(KeyboardListener keyboardListener){
        this.keyboardListener = keyboardListener;
    }
}