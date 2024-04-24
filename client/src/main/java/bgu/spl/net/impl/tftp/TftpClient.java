package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

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
        BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<>();
        TftpEncoderDecoder encoderDecoder = new TftpEncoderDecoder();
        TftpClientProtocol protocol = new TftpClientProtocol();
        Socket sock;
        try {
            sock = new Socket(args[0], Integer.parseInt(args[1]));
        } catch (IOException e) {
            System.out.println("cant create socket");
            return;
        }
        Listener listener = new Listener(messageQueue, sock, encoderDecoder);
        KeyboardListener keyboardListener = new KeyboardListener(messageQueue, protocol, listener, sock, encoderDecoder);
        listener.setKeyboardListener(keyboardListener);
        Thread ListeningThread = new Thread(listener);
        Thread KeyboardThread = new Thread(keyboardListener);
        KeyboardThread.start();
        ListeningThread.start();
        ListeningThread.join();
    }
}
