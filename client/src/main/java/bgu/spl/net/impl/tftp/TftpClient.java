package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.*;
import java.net.Socket;

public class TftpClient {
    public static void main(String[] args) throws InterruptedException {
        MessageEncoderDecoder<byte[]> encoderDecoder = new TftpEncoderDecoder();
        TftpClientProtocol protocol = new TftpClientProtocol();
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        try (Socket sock = new Socket(host, port);
            BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream())) {

            Listener listener = new Listener(encoderDecoder, in,out, protocol);
            KeyboardListener keyboardListener = new KeyboardListener(encoderDecoder, protocol, out);
            listener.setKeyboardListener(keyboardListener);
            Thread ListeningThread = new Thread(listener);
            Thread KeyboardThread = new Thread(keyboardListener);
            KeyboardThread.start();
            ListeningThread.start();
            ListeningThread.join();
        }
        catch (IOException | InterruptedException e) {
            System.out.println(e.getCause());
            throw new RuntimeException(e);
        }
    }
}
