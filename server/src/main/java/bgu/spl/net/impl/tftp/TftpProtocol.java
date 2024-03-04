package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private boolean terminate;

    public TftpProtocol(){
        this.terminate = false;
    }
    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void process(byte[] message) {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    } 


    
}
