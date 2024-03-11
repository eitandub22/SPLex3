package bgu.spl.net.impl.tftp;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class KeyboardListener implements Runnable{
    private boolean run = true;
    private BlockingQueue<String> messageQueue;
    private Scanner scanner;
    private final String LOGRQ = "LOGRQ";
    private final String DELRQ = "DELRQ";
    private final String RRQ = "RRQ";
    private final String WRQ = "WRQ";
    private final String DIRQ = "DIRQ";
    private final String DISC = "DISC";
    Set<String> userCommands = new HashSet<>();
    String[] commands = new String[]{LOGRQ, DELRQ, RRQ, WRQ, DIRQ, DISC};

    public KeyboardListener(BlockingQueue<String> messageQueue){
        this.messageQueue = messageQueue;
        this.scanner = new Scanner(System.in);
        Collections.addAll(this.userCommands, this.commands);
    }
    @Override
    public void run() {
        while(run){
            String message = scanner.next();
            handleMessage(message);
        }
    }

    private void handleMessage(String message){
        String command = message.split(" ")[0];
        if(userCommands.contains(command)){
            switch (command){
                case LOGRQ:

                    break;
                case DELRQ:
                    break;
                case RRQ:
                    break;
                case WRQ:
                    break;
                case DIRQ:
                    break;
                case DISC:
                    break;
            }
        }
        else{
            synchronized (System.out){
                System.out.println("The command you enter is illegal");
            }
        }
    }
}
