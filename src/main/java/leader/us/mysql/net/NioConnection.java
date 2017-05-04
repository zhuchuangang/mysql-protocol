package leader.us.mysql.net;

import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/2.
 */
public class NioConnection {
    protected SocketChannel socketChannel;

    public NioConnection(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
}
