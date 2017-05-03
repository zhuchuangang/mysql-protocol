package leader.us.mysql.net;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/2.
 */
public class BackendHandler extends NioHandler {
    public BackendHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        super(selector, socketChannel);
    }

    @Override
    public void run() {

    }
}
