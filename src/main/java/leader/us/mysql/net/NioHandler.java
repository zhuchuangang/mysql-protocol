package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zcg on 2017/5/2.
 */
public abstract class NioHandler implements Runnable {

    private static Logger logger = LogManager.getLogger(NioHandler.class);
    protected static final int MAX_READ = 1024;
    protected DirectByteBufferPool bufferPool;
    protected final Selector selector;
    protected final NioConnection connection;
    protected SelectionKey selectionKey;
    protected ByteBuffer readBuffer;
    //protected ByteBuffer writeBuffer;
    protected Chunk writeChunk;

    private volatile LinkedList<Chunk> bufferQueue = new LinkedList<>();

    private AtomicBoolean writeFlag = new AtomicBoolean(false);


    public NioHandler(Selector selector, NioConnection connection, DirectByteBufferPool bufferPool) throws IOException {
        this.bufferPool = bufferPool;
        this.readBuffer = ByteBuffer.allocateDirect(MAX_READ);
        this.selector = selector;
        this.selector.wakeup();
        this.connection = connection;
        //设置为非阻塞状态
        this.connection.getSocketChannel().configureBlocking(false);
        //注册写事件
        //TODO 使用select(timeout),register方法阻塞几秒，如何解决？
        //TODO 使用select(),register方法阻塞死掉，如何解决？
        //http://tool.oschina.net/apidocs/apidoc?api=jdk-zh
        //如果一个selection thread已经在select方法上等待ing,那么这个时候如果有另一条线程调用channal.register方法的话,那么它将被blocking.
        //http://blog.csdn.net/chenxuegui1234/article/details/17766813
        this.selectionKey = this.connection.getSocketChannel().register(selector, SelectionKey.OP_READ);
        this.selectionKey.attach(this);
        this.onConnection(this.connection.getSocketChannel());
    }

    public void onConnection(SocketChannel socketChannel) throws IOException {
        logger.info("{} connection has to accept,socket channel is {}", Thread.currentThread().getName(), socketChannel);
    }

    public void doReadData() throws IOException {
        throw new IOException("If don`t implement NioHandler.doReadData() method,you can`t read socket data!");
    }

    public void doWriteData() throws IOException {
        while (!writeFlag.compareAndSet(false, true)) {
            //until the release
            //System.out.println("doWriteData:until the release");
        }
        try {
            writeToChannel(writeChunk);
        } finally {
            writeFlag.lazySet(false);
        }
    }


    public void writeData(Chunk chunk) throws IOException {
        while (!writeFlag.compareAndSet(false, true)) {
            //until the release
            //System.out.println("writeData:until the release");
        }
        try {
            if (writeChunk == null && bufferQueue.isEmpty()) {
                //System.out.println("writeBuffer is null,bufferQueue is empty,and write buffer");
                writeToChannel(chunk);
            } else {
                //System.out.println("bufferQueue add buffer,and write writeBuffer");
                bufferQueue.add(chunk);
                writeToChannel(writeChunk);
            }
        } finally {
            writeFlag.lazySet(false);
        }
    }


    public void writeToChannel(Chunk chunk) throws IOException {
        int writeNum = this.connection.getSocketChannel().write(chunk.getBuffer());
        //System.out.println("write num:" + writeNum);
        if (chunk.getBuffer().hasRemaining()) {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            this.selector.wakeup();
            if (writeChunk != chunk) {
                writeChunk = chunk;
            } else {
                bufferPool.recycleChunk(chunk);
            }
        } else {
            //System.out.println("finish writing byteBuffer");
            if (bufferQueue.isEmpty()) {
                //System.out.println("bufferQueue is empty");
                selectionKey.interestOps(SelectionKey.OP_READ);
                this.selector.wakeup();
            } else {
                //System.out.println("write bufferQueue");
                Chunk curChunk = bufferQueue.removeFirst();
                writeToChannel(curChunk);
            }
        }
    }

}
