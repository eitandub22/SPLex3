package bgu.spl.net.srv;

<<<<<<< HEAD
=======

>>>>>>> 9d53931336c8e68b53e675dfc0e08e4348a400f4

public interface Connections<T> {

    void connect(int connectionId, ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    void disconnect(int connectionId);
}
