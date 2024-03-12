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
        boolean run = true;
        BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<>();
        MessageEncoderDecoder<byte[]> encoderDecoder = new TftpEncoderDecoder();
        TftpClientProtocol protocol = new TftpClientProtocol();
        Scanner scanner = new Scanner(System.in);
        Listener listener = new Listener(messageQueue, args[0], encoderDecoder);
        Thread ListeningThread = new Thread(listener);
        Thread KeyboardThread = new Thread(new KeyboardListener(messageQueue, protocol, listener));
        KeyboardThread.run();
        ListeningThread.run();
    }
}
