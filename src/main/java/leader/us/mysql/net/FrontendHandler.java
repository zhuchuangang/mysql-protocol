package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.constants.StatusFlags;
import leader.us.mysql.protocol.packet.AuthPacket;
import leader.us.mysql.protocol.packet.HandshakePacket;
import leader.us.mysql.protocol.packet.OKPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/3/25.
 */
public class FrontendHandler extends NioHandler {

    private static Logger logger = LogManager.getLogger(FrontendHandler.class);
    private FakeLoginAuthenticationHandler loginAuthenticationHandler;

    public FrontendHandler(Selector selector, SocketChannel socketChannel, DirectByteBufferPool bufferPool) throws IOException {
        super(selector, socketChannel, bufferPool);
        loginAuthenticationHandler = new FakeLoginAuthenticationHandler(bufferPool);
    }

    @Override
    public void run() {
        try {
            if (this.selectionKey.isReadable()) {
                doReadData();
            } else if (this.selectionKey.isWritable()) {
                doWriteData();
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void onConnection(SocketChannel socketChannel) throws IOException {
        super.onConnection(socketChannel);
        HandshakePacket handshake = FakeMysqlServer.getInstance().response();
        logger.info(handshake);
        Chunk chunk = bufferPool.getChunk(handshake.calcPacketSize() + 4);
        handshake.write(chunk.getBuffer());
        chunk.getBuffer().flip();
        writeData(chunk);
    }

    @Override
    public void doReadData() throws IOException {
        Chunk chunk = bufferPool.getChunk(1024);
        int readNum = this.socketChannel.read(chunk.getBuffer());
        chunk.getBuffer().flip();
        if (readNum == 0) {
            return;
        }
        if (readNum == -1) {
            socketChannel.socket().close();
            socketChannel.close();
            selectionKey.cancel();
            return;
        }

        chunk = loginAuthenticationHandler.response(chunk, socketChannel);
        writeData(chunk);
    }
}