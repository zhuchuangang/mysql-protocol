package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.packet.*;
import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.MySQLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
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


    /**
     * 将每个packet读取到buffer中，并且一起进行输出
     *
     * @throws IOException
     */
    @Override
    public void doReadData() throws IOException {
        SocketChannel channel = this.connection.getSocketChannel();
        ByteBuffer temp = ByteBuffer.allocate(3);
        MySQLMessage tm = new MySQLMessage(temp);
        int packetLength = 0;
        List<Chunk> chunks = new ArrayList<>();
        while (true) {
            temp.clear();
            int readNum = channel.read(temp);
            logger.debug("1.read packet length byte is " + readNum);
            if (readNum == 0) {
                break;
            }
            if (readNum == -1) {
                this.connection.getSocketChannel().socket().close();
                this.connection.getSocketChannel().close();
                selectionKey.cancel();
                break;
            }
            temp.flip();
            packetLength = tm.readUB3();
            logger.debug("2.read packet length is " + packetLength);

            Chunk chunk = bufferPool.getChunk(packetLength + 4);
            ByteBuffer buffer = chunk.getBuffer();
            int limit = buffer.limit();
            int position = buffer.position();
            if (limit - position > packetLength + 4) {
                int offset = (limit - position) - (packetLength + 4);
                chunk.getBuffer().limit(limit - offset);
            }
            BufferUtil.writeUB3(chunk.getBuffer(), packetLength);

            readNum = channel.read(chunk.getBuffer());
            logger.debug("4.packetLength is " + packetLength + ",read packet body is " + readNum + ",chuck buffer size is " + chunk.getBuffer().capacity());
            if (readNum == 0) {
                break;
            }
            if (readNum == -1) {
                this.connection.getSocketChannel().socket().close();
                this.connection.getSocketChannel().close();
                selectionKey.cancel();
                break;
            }
            chunk.getBuffer().flip();
            chunks.add(chunk);
        }
        if (frontendHandler != null && !chunks.isEmpty()) {
            //MysqlResponseHandler.dump(chunk.getBuffer(), frontendHandler);
            Chunk[] c = chunks.toArray(new Chunk[chunks.size()]);
            frontendHandler.writeData(c);
        }
    }

//    @Override
//    public void doReadData() throws IOException {
//        SocketChannel channel = this.connection.getSocketChannel();
//        ByteBuffer temp = ByteBuffer.allocate(8 + 3);
//        temp.limit(5);
//        int readNum = channel.read(temp);
//        temp.flip();
//        logger.debug("read num is {}", readNum);
//        if (readNum == 0) {
//            return;
//        }
//        if (readNum == 4) {
//            return;
//        }
//        if (readNum == -1) {
//            this.connection.getSocketChannel().socket().close();
//            this.connection.getSocketChannel().close();
//            selectionKey.cancel();
//            return;
//        }
//        MySQLMessage tm = new MySQLMessage(temp);
//        //包长度
//        int packetLength = tm.readUB3();
//        tm.move(1);
//        //包类型
//        byte packetType = tm.read();
//        //动态buffer
//        Chunk chunk = null;
//        ByteBuffer buffer = null;
//        //buffer的position和limit
//        int position = 0;
//        int limit = 0;
//
//        logger.debug("packet length is {}", packetLength);
//
//        switch (packetType) {
//            //OK packet
//            case 0x00:
//                //LOCAL_INFILE packet
//            case (byte) 0xfb:
//                //EOF packet
//            case (byte) 0xfe:
//                //ERROR packet
//            case (byte) 0xff:
//                chunk = bufferPool.getChunk(packetLength + 4);
//                buffer = chunk.getBuffer();
//                limit = buffer.limit();
//                position = buffer.position();
//                if (limit - position > packetLength + 4) {
//                    int offset = (limit - position) - (packetLength + 4);
//                    chunk.getBuffer().limit(limit - offset);
//                }
//                BufferUtil.writeUB3(buffer, packetLength);
//                buffer.put(temp.get(3));
//                buffer.put(packetType);
//                channel.read(buffer);
//                break;
//            //ResultSet
//            default:
//                //获取字段个数
//                long columnsNumber = packetType & 0xff;
//                temp.clear();
//                switch ((int) columnsNumber) {
//                    case 251:
//                        columnsNumber = -1;
//                    case 252:
//                        temp.limit(2);
//                        channel.read(temp);
//                        temp.flip();
//                        columnsNumber = tm.readUB2();
//                        break;
//                    case 253:
//                        temp.limit(3);
//                        channel.read(temp);
//                        temp.flip();
//                        columnsNumber = tm.readUB3();
//                        break;
//                    case 254:
//                        temp.limit(8);
//                        channel.read(temp);
//                        temp.flip();
//                        columnsNumber = tm.readLength();
//                        break;
//                }
//                //读取第一个列定义的包长度
//                int p = temp.position();
//                temp.limit(temp.limit() + 3);
//                channel.read(temp);
//                temp.position(p);
//                int cdPacketLength = tm.readUB3();
//                //以第一个列定义估算所有列定义的长度
//                int cdSize = (int) (columnsNumber * (cdPacketLength + 4) * 1.25);
//                chunk = bufferPool.getChunk(4 + 1 + p + cdSize);
//                buffer = chunk.getBuffer();
//                //写入第一个报文
//                BufferUtil.writeUB3(buffer, packetLength);
//                buffer.put(temp.get(3));
//                buffer.put(packetType);
//                BufferUtil.writeLength(buffer, columnsNumber);
//                //读取后续报文
//                int capacity = buffer.capacity();
//
//                for (int i = 0; i < columnsNumber; i++) {
//                    if (i == 0) {
//                        //写入第二报文的长度(第一个列定义)
//                        buffer.limit(cdPacketLength + 4);
//                        BufferUtil.writeUB3(buffer, cdPacketLength);
//                        channel.read(buffer);
//                    } else {
//                        temp.clear();
//                        temp.limit(3);
//                        channel.read(temp);
//                        cdPacketLength = tm.readUB3();
//                        buffer.limit(cdPacketLength + 4);
//                        BufferUtil.writeUB3(buffer, cdPacketLength);
//                        channel.read(buffer);
//                    }
//                }
//        }
//
//        //=====================
//        StringBuffer sb = new StringBuffer("\n");
//        for (int i = position; i < chunk.getBuffer().limit(); i++) {
//            String t = Integer.toHexString(chunk.getBuffer().get(i) & 0xff);
//            if (t.length() == 1) {
//                t = "0" + t;
//            }
//            sb.append(t + " ");
//            if ((i + 1) % 8 == 0) {
//                sb.append(" ");
//            }
//            if ((i + 1) % 16 == 0) {
//                sb.append("\n");
//            }
//        }
//        logger.debug(sb.toString());
//        //=====================
//
//        if (chunk != null) {
//            chunk.getBuffer().position(position);
//            if (frontendHandler != null) {
//                //MysqlResponseHandler.dump(chunk.getBuffer(), frontendHandler);
//                frontendHandler.writeData(chunk);
//            }
//        }
//    }


//    @Override
//    public void doReadData() throws IOException {
//        SocketChannel channel = this.connection.getSocketChannel();
//        ByteBuffer temp = ByteBuffer.allocate(8);
//        MySQLMessage tm = new MySQLMessage(temp);
//        int packetLength = 0;
//        List<Chunk> chunks = new ArrayList<>();
//        boolean firstPacket = true;
//        boolean resultSet = false;
//        byte packetType = 0;
//        while (true) {
//            //===============================================================
//            //=====================读取packetLength===========================
//            //===============================================================
//            temp.clear();
//            int readNum = channel.read(temp);
//            logger.debug("1.read packet length byte is " + readNum);
//            if (checkReadNum(readNum)) {
//                break;
//            }
//            temp.flip();
//            packetLength = tm.readUB3();
//            logger.debug("2.read packet length is " + packetLength);
//
//            //===============================================================
//            //=====================读取packetType=============================
//            //===============================================================
//            if (firstPacket) {
//                firstPacket = false;
//                temp.clear();
//                temp.limit(2);
//                readNum = channel.read(temp);
//                if (checkReadNum(readNum)) {
//                    break;
//                }
//                packetType = temp.get(1);
//                //OK packet  0x00
//                //LOCAL_INFILE packet 0xfb
//                //EOF packet  0xfe
//                //ERROR packet  0xff
//                if ((packetType == 0x00 && packetLength > 7) ||
//                        (packetType == 0xfe && packetLength == 5) ||
//                        packetType == 0xff ||
//                        packetType == 0xfb) {
//                    resultSet = false;
//                } else {
//                    resultSet = true;
//                }
//            }
//
//            //===============================================================
//            //===================读取columnsCount=============================
//            //===============================================================
//            long columnsCount = resultSet ? packetType & 0xff : 0;
//            if (resultSet) {
//                //读取第一个列定义的包长度
//                temp.clear();
//                temp.limit(packetLength);
//                temp.put((byte) columnsCount);
//                readNum = channel.read(temp);
//                if (checkReadNum(readNum)) {
//                    break;
//                }
//                columnsCount = tm.readLength();
//            }
//            //假设列定义的包平均长度为50，1.25为弹性系数
//            //packetLength+4为第一个包的长度
//            int bufferSize = (int) (packetLength + 4 + columnsCount * 50 * 1.25);
//            //===============================================================
//            //========================读取剩余信息=============================
//            //===============================================================
//            Chunk chunk = bufferPool.getChunk(bufferSize + 4);
//            ByteBuffer buffer = chunk.getBuffer();
//            int limit = buffer.limit();
//            int position = buffer.position();
//            if (limit - position > packetLength + 4) {
//                int offset = (limit - position) - (packetLength + 4);
//                chunk.getBuffer().limit(limit - offset);
//            }
//            BufferUtil.writeUB3(chunk.getBuffer(), packetLength);
//
//            readNum = channel.read(chunk.getBuffer());
//            logger.debug("4.packetLength is " + packetLength + ",read packet body is " + readNum + ",chuck buffer size is " + chunk.getBuffer().capacity());
//            if (readNum == 0) {
//                break;
//            }
//            if (readNum == -1) {
//                this.connection.getSocketChannel().socket().close();
//                this.connection.getSocketChannel().close();
//                selectionKey.cancel();
//                break;
//            }
//            chunk.getBuffer().flip();
//            chunks.add(chunk);
//        }
//        if (frontendHandler != null && !chunks.isEmpty()) {
//            //MysqlResponseHandler.dump(chunk.getBuffer(), frontendHandler);
//            Chunk[] c = chunks.toArray(new Chunk[chunks.size()]);
//            frontendHandler.writeData(c);
//        }
//    }
//
//    public boolean checkReadNum(int readNum) throws IOException {
//        if (readNum == 0) {
//            return true;
//        }
//        if (readNum == -1) {
//            this.connection.getSocketChannel().socket().close();
//            this.connection.getSocketChannel().close();
//            selectionKey.cancel();
//            return true;
//        }
//        return false;
//    }


    public void setFrontendHandler(FrontendHandler frontendHandler) {
        this.frontendHandler = frontendHandler;
    }
}
