package bgu.spl.net.impl.tftp;

import java.util.Map;
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

    public boolean logout(int connectionId){
        for(Map.Entry<String, Integer> user : loggedUsers.entrySet()){
            if(user.getValue() == connectionId){
                loggedUsers.remove(user.getKey());
                return true;
            }
        }
        return false;
    }

    public boolean isLoggedIn(int connectionId) {
        for(Map.Entry<String, Integer> user : loggedUsers.entrySet()){
            if(user.getValue() == connectionId){
                return true;
            }
        }
        return false;
    }
    //TODO: Implement this
}
