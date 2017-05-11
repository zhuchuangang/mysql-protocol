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
    private SqlCommandHandler commandHandler;

    public FrontendHandler(Selector selector, FrontendConnection connection, DirectByteBufferPool bufferPool) throws IOException {
        super(selector, connection, bufferPool);
        this.commandHandler = new FakeLoginAuthenticationHandler(bufferPool);
    }

    @Override
    public void run() {
        if (!this.selectionKey.isValid()) {
            logger.debug("select-key cancelled");
            return;
        }
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
        int readNum = this.connection.getSocketChannel().read(chunk.getBuffer());
        chunk.getBuffer().flip();
        if (readNum == 0) {
            return;
        }
        if (readNum == -1) {
            this.connection.getSocketChannel().socket().close();
            this.connection.getSocketChannel().close();
            selectionKey.cancel();
            return;
        }

        chunk = commandHandler.response(chunk, this.connection.getSocketChannel(), this);
        if (chunk != null) {
            writeData(chunk);
        }
    }

    public void setCommandHandler(SqlCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }
}
