package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl implements Connections<byte[]>{
    private ConcurrentHashMap<Integer, ConnectionHandler<byte[]>> activeConnections;
    @Override
    public void connect(int connectionId, ConnectionHandler<byte[]> handler) {

    }

    @Override
    public boolean send(int connectionId, byte[] msg) {
        return false;
    }

    @Override
    public void disconnect(int connectionId) {

    }
}
