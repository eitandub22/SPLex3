package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.packets.PacketFactory;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class KeyboardListener implements Runnable{
    private boolean run = true;
    private BlockingQueue<byte[]> messageQueue;
    private Scanner scanner;
    private TftpClientProtocol protocol;

    public KeyboardListener(BlockingQueue<byte[]> messageQueue, TftpClientProtocol protocol){
        this.messageQueue = messageQueue;
        this.scanner = new Scanner(System.in);
        this.protocol = protocol;

    }
    @Override
    public void run() {
        while(run){
            String message = scanner.next();
            try {
                messageQueue.put(protocol.process(message.getBytes()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
