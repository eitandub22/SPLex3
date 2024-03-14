package bgu.spl.net.impl.tftp;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.tftp.packets.packetReaders.WRQreader;

public class KeyboardListener implements Runnable{
    private boolean run = true;
    private Scanner scanner;
    private TftpClientProtocol protocol;
    BufferedOutputStream out;
    MessageEncoderDecoder<byte[]> encoderDecoder;

    public KeyboardListener(MessageEncoderDecoder<byte[]> encoderDecoder, TftpClientProtocol protocol, BufferedOutputStream out){
        this.scanner = new Scanner(System.in);
        this.encoderDecoder = encoderDecoder;
        this.protocol = protocol;
        this.out = out;
    }
    @Override
    public void run() {
        while(run){
            System.out.print("Enter Command: ");
            String message = scanner.nextLine();
            try {
                byte[] currentMessage = protocol.process(message.getBytes(StandardCharsets.UTF_8));
                if (currentMessage.length == 1) {
                    switch (currentMessage[0]) {
                        case 0://invalid input error
                            System.out.println("Input is invalid");
                            continue;
                        case 1://file exists error
                            System.out.println("file already exists");
                            continue;
                        case 2://file not exists error
                            System.out.println("file does not exists");
                            continue;
                    }
                }
                out.write(encoderDecoder.encode(currentMessage));
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void terminate() {
        this.run = false;
    }
}
