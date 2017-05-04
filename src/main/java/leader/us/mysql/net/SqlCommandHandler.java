package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;

import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/4.
 */
public interface SqlCommandHandler {
    Chunk response(Chunk chunk, SocketChannel socketChannel,FrontendHandler handler);
}
