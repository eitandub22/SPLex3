package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.packets.PacketFactory;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class KeyboardListener implements Runnable{
    private boolean run = true;
    private BlockingQueue<byte[]> messageQueue;
    private Scanner scanner;
    private TftpClientProtocol protocol;
    Listener listener;

    public KeyboardListener(BlockingQueue<byte[]> messageQueue, TftpClientProtocol protocol, Listener listener){
        this.messageQueue = messageQueue;
        this.scanner = new Scanner(System.in);
        this.protocol = protocol;
        this.listener = listener;

    }
    @Override
    public void run() {
        while(run){
            System.out.print("Enter Command: ");
            String message = scanner.nextLine();
            try {
                messageQueue.put(protocol.process(message.getBytes()));
<<<<<<< HEAD
                synchronized(listener){listener.wait();}
=======
                synchronized (listener){
                    listener.wait();
                }
>>>>>>> 7d16dca61cf42e7adfbdb6b0c9ff900e657b9675
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void terminate() {
        this.run = false;
    }
}
