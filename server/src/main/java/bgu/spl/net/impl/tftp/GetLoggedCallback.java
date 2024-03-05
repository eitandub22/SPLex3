package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

public interface GetLoggedCallback {
    public ConcurrentHashMap<String, Integer> getLoggedIn();
}
