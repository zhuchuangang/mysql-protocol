package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.packet.HandshakePacket;
import leader.us.mysql.protocol.packet.StmtPrepareOKPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/2.
 */
public class BackendHandler extends NioHandler {

    private static Logger logger = LogManager.getLogger(BackendHandler.class);
    private FrontendHandler frontendHandler;

    public BackendHandler(Selector selector, BackendConnection connection, DirectByteBufferPool bufferPool) throws IOException {
        super(selector, connection, bufferPool);
        connection.setBackendHandler(this);
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
        logger.info("{} connection is connected,socket channel is {}", Thread.currentThread().getName(), socketChannel);
        BackendConnection c = (BackendConnection) connection;
        Chunk chunk = c.authentication();
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

        if (frontendHandler != null) {
            if (frontendHandler.getSession().getStmtPrepare()) {
                StmtPrepareOKPacket sp = new StmtPrepareOKPacket();
                sp.read(chunk.getBuffer());
                frontendHandler.getSession().getStmtIdParamCount().put(sp.statementId, sp.parametersNumber);
                chunk.getBuffer().position(0);
                frontendHandler.getSession().setStmtPrepare(false);
            }
            frontendHandler.writeData(chunk);
        }
    }

    public void setFrontendHandler(FrontendHandler frontendHandler) {
        this.frontendHandler = frontendHandler;
    }
}
