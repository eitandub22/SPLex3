package bgu.spl.net.impl.tftp;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

public class Listener implements Runnable{
    private BlockingQueue<String> messageQueue;
    private String host;

    public Listener(BlockingQueue<String> messageQueue, String host){
        this.messageQueue = messageQueue;
        this.host = host;
    }
    @Override
    public void run() {
        try (Socket sock = new Socket(host, 7777);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
            while(!Thread.currentThread().isInterrupted()){
                String currentMessage = messageQueue.take();
                //handle message received from keyboard
                    /*out.write(args[1]); write to server
                    out.newLine();
                    out.flush();*/

                System.out.println("awaiting response");
                String line = in.readLine();
                System.out.println("message from server: " + line);

            }
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
