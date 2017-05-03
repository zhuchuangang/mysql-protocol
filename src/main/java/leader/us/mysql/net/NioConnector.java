package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.packet.HandshakePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zcg on 2017/5/2.
 */
public class NioConnector extends Thread {

    private static Logger logger = LogManager.getLogger(NioConnector.class);
    private DirectByteBufferPool pool;
    private NioReactor[] reactors;
    private SocketChannel socketChannel;
    private Selector selector;
    private String host;
    private int port;
    private String database;
    private HandshakePacket handshake;


    public NioConnector(String host, int port, String database, NioReactor[] reactors) throws IOException {
        this.pool = DirectByteBufferPool.getInstance();
        this.host = host;
        this.port = port;
        this.database = database;
        this.reactors = reactors;
        this.selector = Selector.open();
        this.socketChannel = SocketChannel.open();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    @Override
    public void run() {
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            this.socketChannel.connect(address);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Set<SelectionKey> selectionKeys;
        while (!Thread.interrupted()) {
            try {
                int selectNum = selector.select(1000);
                if (selectNum == 0) {
                    continue;
                }
                selectionKeys = selector.selectedKeys();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isValid() && key.isConnectable()) {
                    try {
                        if (socketChannel.isConnectionPending()) {
                            socketChannel.finishConnect();
                        }
                        Chunk chunk = pool.getChunk(300);
                        socketChannel.read(chunk.getBuffer());
                        handshake = new HandshakePacket();
                        handshake.read(chunk.getBuffer());
                        pool.recycleChunk(chunk);
                        logger.info(handshake);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int index = ThreadLocalRandom.current().nextInt(reactors.length);
                    BackendConnection connection = new BackendConnection(socketChannel);
                    reactors[index].postRegister(connection);
                }
                iterator.remove();
            }
            selectionKeys.clear();
        }
    }


    public void authorization() {
//        AuthPacket ap = new AuthPacket();
//
//        ByteBuffer bb = pool.getChunk()
    }
}
