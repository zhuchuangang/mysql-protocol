package leader.us.mysql.net;

import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/2.
 */
public class BackendConnection extends NioConnection {
    public BackendConnection(SocketChannel socketChannel) {
        super(socketChannel);
    }
}
