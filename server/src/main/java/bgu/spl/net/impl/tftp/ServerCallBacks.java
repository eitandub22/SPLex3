package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

public class ServerCallBacks {
    private LoginCallback loginCallback;
    private GetLoggedCallback getLoggedCallback;

    public ServerCallBacks(TftpServer server){
        this.loginCallback = (int connectionId, String userName) -> server.login(connectionId, userName);//TODO implement
        this.getLoggedCallback = () -> server.getLoggedIn();//TODO implement
    }

    public boolean login(int connectionId, String userName){
        return this.loginCallback.login(connectionId, userName);
    }

    public ConcurrentHashMap<String, Integer> getLoggedIn(){
        return getLoggedCallback.getLoggedIn();
    }
}
