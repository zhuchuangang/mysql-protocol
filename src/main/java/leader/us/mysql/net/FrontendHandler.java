package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
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
    private int readLastPos;

    public FrontendHandler(DirectByteBufferPool bufferPool, Selector selector, SocketChannel socketChannel) throws IOException {
        super(selector, socketChannel, bufferPool);
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
            e.printStackTrace();
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
        if (readNum == -1) {
            socketChannel.socket().close();
            socketChannel.close();
            selectionKey.cancel();
            return;
        }

        AuthPacket ap = new AuthPacket();
        ap.read(chunk.getBuffer());
        bufferPool.recycleChunk(chunk);
        logger.info(ap);

        OKPacket op=new OKPacket();
        op.capabilities=FakeMysqlServer.getFakeServerCapabilities();
        chunk = bufferPool.getChunk(1024);

        writeData(chunk);

//        String readLine = null;
//        int readNum = this.socketChannel.read(this.readBuffer);
//        System.out.println("readNum=" + readNum);
//        //当channel读取到流的末尾是返回-1
//        if (readNum == -1) {
//            //注销通道的读事件
//            this.socketChannel.register(this.selector, 0);
//            //删除附件
//            this.selectionKey.attach(null);
//            return;
//        }
//        int messageLastPos = this.readBuffer.position();
//        int readStartPos = readLastPos;
//        for (int i = readStartPos; i < messageLastPos; i++) {
//            readLastPos = i;
//            //回车符
//            if (this.readBuffer.get(i) == 13) {
//                byte[] temp = new byte[readLastPos - readStartPos];
//                this.readBuffer.position(readStartPos);
//                this.readBuffer.get(temp, 0, temp.length);
//                readLine = new String(temp);
//                break;
//            }
//        }
//        if (readLine != null) {
//            String result = processCommand(readLine);
//            Chunk chunk = bufferPool.getChunk(result.length());
//            writeData(chunk);
//        }
//        //压缩字符串
//        if (readBuffer.position() > readBuffer.capacity() / 2) {
//            readBuffer.limit(readLastPos);
//            readBuffer.compact();
//        }

    }

    private String processCommand(String cmd) {
        String result = LocalCmdUtil.callCmdAndGetResult(cmd);
        result += "\r\ntelnet>";
        return result;
    }
}