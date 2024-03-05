package bgu.spl.net.srv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Connections<T> {

    void connect(int connectionId, ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    void disconnect(int connectionId);

    ConcurrentHashMap<Integer, String> getLoggedIn();

    boolean logIn(int connectionId, String userName);

    Set<Integer> getIds();
}
