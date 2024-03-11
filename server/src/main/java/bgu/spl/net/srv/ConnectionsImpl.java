package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T>{
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> activeConnections;
    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        this.activeConnections.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = this.activeConnections.get(connectionId);
        return handler.send(msg);
    }

    @Override
    public void disconnect(int connectionId) {
        this.activeConnections.remove(connectionId);
    }
}
