package leader.us.mysql.net;

import leader.us.mysql.bufferpool.DirectByteBufferPool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/2.
 */
public class BackendHandler extends NioHandler {

    public BackendHandler(DirectByteBufferPool bufferPool,Selector selector, SocketChannel socketChannel) throws IOException {
        super(selector, socketChannel,bufferPool);
    }

    @Override
    public void run() {

    }
}
