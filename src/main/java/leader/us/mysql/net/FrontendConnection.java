package leader.us.mysql.net;

import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/2.
 */
public class FrontendConnection extends NioConnection {
    public FrontendConnection(SocketChannel socketChannel) {
        super(socketChannel);
    }
}
