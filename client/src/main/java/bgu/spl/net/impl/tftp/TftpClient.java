package bgu.spl.net.impl.tftp;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TftpClient {
    public static void main(String[] args) throws InterruptedException {
        boolean run = true;
        BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
        Scanner scanner = new Scanner(System.in);
        Thread ListeningThread = new Thread(new Listener(messageQueue, args[0]));
        Thread KeyboardThread = new Thread(new KeyboardListener(messageQueue));
        KeyboardThread.run();
        ListeningThread.run();
    }
}
