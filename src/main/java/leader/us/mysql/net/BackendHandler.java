package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.support.BufferUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

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

//    @Override
//    public void doReadData() throws IOException {
//        Chunk chunk = bufferPool.getChunk(1024);
//        int readNum = this.connection.getSocketChannel().read(chunk.getBuffer());
//        chunk.getBuffer().flip();
//
//        if (readNum == 0) {
//            return;
//        }
//        if (readNum == -1) {
//            this.connection.getSocketChannel().socket().close();
//            this.connection.getSocketChannel().close();
//            selectionKey.cancel();
//            return;
//        }
//
//        if (frontendHandler != null) {
//            MysqlResponseHandler.dump(chunk.getBuffer(), frontendHandler);
//            frontendHandler.writeData(chunk);
//        }
//    }

    private volatile int packetLength = 0;
    private byte[] lengthByte;

    @Override
    public void doReadData() throws IOException {
        SocketChannel channel = this.connection.getSocketChannel();
        lengthByte = new byte[3];
        ByteBuffer b = ByteBuffer.wrap(lengthByte);
        if (packetLength == 0) {
            int readNum = channel.read(b);
            logger.debug("1.read packet length byte is " + readNum);
            if (readNum == 0) {
                return;
            }
            if (readNum == -1) {
                this.connection.getSocketChannel().socket().close();
                this.connection.getSocketChannel().close();
                selectionKey.cancel();
                return;
            }
            int length = lengthByte[0] & 0xff;
            length |= (lengthByte[1] & 0xff) << 8;
            length |= (lengthByte[2] & 0xff) << 16;
            packetLength = length;
            logger.debug("2.read packet length is " + packetLength);
            return;
        } else {
            logger.debug("3.packetLength is " + packetLength);
            Chunk chunk = bufferPool.getChunk(packetLength + 4);
            BufferUtil.writeUB3(chunk.getBuffer(), packetLength);
            packetLength = 0;

            int readNum = channel.read(chunk.getBuffer());
            logger.debug("4.packetLength is " + packetLength + ",read packet body is " + readNum + ",chuck buffer size is " + chunk.getBuffer().capacity());
            if (readNum == 0) {
                return;
            }
            if (readNum == -1) {
                this.connection.getSocketChannel().socket().close();
                this.connection.getSocketChannel().close();
                selectionKey.cancel();
                return;
            }

            chunk.getBuffer().flip();
            if (frontendHandler != null) {
                //MysqlResponseHandler.dump(chunk.getBuffer(), frontendHandler);
                frontendHandler.writeData(chunk);
            }
        }
    }

    public void setFrontendHandler(FrontendHandler frontendHandler) {
        this.frontendHandler = frontendHandler;
    }
}
