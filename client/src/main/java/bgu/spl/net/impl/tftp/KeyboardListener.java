package bgu.spl.net.impl.tftp;
import java.io.BufferedOutputStream;
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

    public KeyboardListener(BlockingQueue<byte[]> messageQueue, TftpClientProtocol protocol, Listener listener){
        this.messageQueue = messageQueue;
        this.scanner = new Scanner(System.in);
        this.protocol = protocol;
        this.listener = listener;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try(BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());){
            while(run){
                System.out.print("Enter Command: ");
                String message = scanner.nextLine();
                out.write(protocol.process(message.getBytes(StandardCharsets.UTF_8)));
                out.flush();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            scanner.close();
        }
    }

    public void terminate() {
        this.run = false;
    }
}
