package bgu.spl.net.impl.tftp;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class KeyboardListener implements Runnable{
    private boolean run = true;
    private BlockingQueue<byte[]> messageQueue;
    private Scanner scanner;
    private TftpClientProtocol protocol;
    final Listener listener;
    private Socket socket;
    private TftpEncoderDecoder encoderDecoder;

    public KeyboardListener(BlockingQueue<byte[]> messageQueue, TftpClientProtocol protocol, Listener listener, Socket socket, TftpEncoderDecoder encoderDecoder){
        this.messageQueue = messageQueue;
        this.scanner = new Scanner(System.in);
        this.protocol = protocol;
        this.listener = listener;
        this.socket = socket;
        this.encoderDecoder = encoderDecoder;
    }
    @Override
    public void run() {
        while(run){
            System.out.print("Enter Command: ");
            String message = scanner.nextLine();
            try(BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());) {
                byte[] currentMessage = protocol.process(message.getBytes(StandardCharsets.UTF_8));
                if (currentMessage.length == 1) {
                    switch (currentMessage[0]) {
                        case 0://invalid input error
                            System.out.println("Input is invalid");
                            synchronized (this){
                                this.notifyAll();
                            }
                            continue;
                        case 1://file exists error
                            System.out.println("file already exists");
                            synchronized (this){
                                this.notifyAll();
                            };
                            continue;
                        case 2://file not exists error
                            System.out.println("file does not exists");
                            synchronized (this){
                                this.notifyAll();
                            };
                            continue;
                    }
                }
                out.write(encoderDecoder.encode(currentMessage));
                out.flush();
                synchronized (listener){
                    listener.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void terminate() {
        this.run = false;
    }
}
