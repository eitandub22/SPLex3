package bgu.spl.net.impl.tftp;

public interface LoginCallback {
    public boolean login(int connectionId, String userName);
}
