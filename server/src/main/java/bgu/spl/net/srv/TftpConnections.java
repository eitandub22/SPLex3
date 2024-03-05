package bgu.spl.net.srv;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TftpConnections implements Connections<Byte>{
    ConcurrentHashMap<Integer, String> loggedUsers;
    ConcurrentHashMap<Integer, ConnectionHandler<Byte>> connections;
    @Override
    public void connect(int connectionId, ConnectionHandler<Byte> handler) {

    }

    @Override
    public boolean send(int connectionId, Byte msg) {
        return false;
    }

    @Override
    public void disconnect(int connectionId) {

    }

    @Override
    public ConcurrentHashMap<Integer, String> getLoggedIn() {
        return null;
    }

    @Override
    public boolean logIn(int connectionId, String userName) {
        return false;
    }

    @Override
    public Set<Integer> getIds() {
        return null;
    }
}
