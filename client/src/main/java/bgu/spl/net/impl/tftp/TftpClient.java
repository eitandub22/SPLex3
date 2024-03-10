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
    private static BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    public static void main(String[] args) throws InterruptedException {
        boolean run = true;

        Scanner scanner = new Scanner(System.in);
        Thread ListeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket sock = new Socket(args[0], 7777);
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
        });

        while(run){
            String currentMessage = scanner.next();
            messageQueue.put(currentMessage);
        }
    }
}
