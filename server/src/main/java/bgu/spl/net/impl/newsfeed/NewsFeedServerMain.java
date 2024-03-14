package bgu.spl.net.impl.newsfeed;
<<<<<<< HEAD
=======


>>>>>>> 9d53931336c8e68b53e675dfc0e08e4348a400f4
public class NewsFeedServerMain {

    public static void main(String[] args) {
        NewsFeed feed = new NewsFeed(); //one shared object

        // you can use any server... 
        /*Server.threadPerClient(
                7777, //port
                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
                ObjectEncoderDecoder::new //message encoder decoder factory
        ).serve();*/
        
    }
}
