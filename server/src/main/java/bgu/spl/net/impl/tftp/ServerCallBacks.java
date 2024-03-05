package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

public class ServerCallBacks {
    private final LoginCallback loginCallback;
    private final GetLoggedCallback getLoggedCallback;
    private final LogoutCallback logoutCallback;
    private final IsLoggedInCallback isLoggedInCallback;

    public ServerCallBacks(TftpServer server){
        this.loginCallback = server::login;//TODO implement
        this.getLoggedCallback = server::getLoggedIn;//TODO implement
        this.logoutCallback = server::logout;
        this.isLoggedInCallback = server::isLoggedIn;
    }

    public boolean login(int connectionId, String userName){
        return this.loginCallback.login(connectionId, userName);
    }

    public ConcurrentHashMap<String, Integer> getLoggedIn(){
        return getLoggedCallback.getLoggedIn();
    }

    public boolean logout(int connectionId){ return this.logoutCallback.logout(connectionId); }

    public boolean isLoggedIn(int connectionId){ return this.isLoggedInCallback.isLoggedIn(connectionId); }
}
