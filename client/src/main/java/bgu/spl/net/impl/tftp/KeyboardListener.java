package bgu.spl.net.impl.tftp;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class KeyboardListener implements Runnable{
    private boolean run = true;
    private BlockingQueue<String> messageQueue;
    private Scanner scanner;

    public KeyboardListener(BlockingQueue<String> messageQueue){
        this.messageQueue = messageQueue;
        this.scanner = new Scanner(System.in);
    }
    @Override
    public void run() {
        while(run){
            String message = scanner.next();
            try {
                messageQueue.put(message);
            } catch (InterruptedException e) {
                continue;
            }
        }
    }
}
