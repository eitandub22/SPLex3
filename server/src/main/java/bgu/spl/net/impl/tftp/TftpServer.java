package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TftpServer implements Server<byte[]>{
    private ConcurrentHashMap<String, Integer> loggedUsers;
    private final int port;
    private final Supplier<BidiMessagingProtocol<byte[]>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<byte[]>> encdecFactory;
    private ServerSocket sock;
    private final Connections<byte[]> connections;
    private int connectionId = 0;
    public TftpServer(int port, Supplier<BidiMessagingProtocol<byte[]>> protocolFactory, Supplier<MessageEncoderDecoder<byte[]>> encdecFactory) {
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        this.connections = new ConnectionsImpl();
    }
    protected void execute(BlockingConnectionHandler<byte[]> handler) {
        new Thread(handler).run();
    }
    @Override
    public void serve() {
        try (ServerSocket serverSock = new ServerSocket(port)) {
            System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                BlockingConnectionHandler<byte[]> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());
                connections.connect(this.connectionId, handler);
                this.connectionId++;
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }
}

