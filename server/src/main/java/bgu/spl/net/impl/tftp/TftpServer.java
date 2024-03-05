package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

public class TftpServer {
    private ConcurrentHashMap<String, Integer> loggedUsers;
    public ConcurrentHashMap<String, Integer> getLoggedIn() {
        return this.loggedUsers;
    }

    public boolean login(int connectionId, String userName) {
        if(loggedUsers.containsKey(userName)) return false;
        else{
            loggedUsers.put(userName, connectionId);
            return true;
        }
    }
    //TODO: Implement this
}
