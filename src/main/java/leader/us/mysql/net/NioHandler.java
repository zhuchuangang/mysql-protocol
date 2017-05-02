package leader.us.mysql.net;

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

    protected static final int MAX_READ = 1024;
    protected final Selector selector;
    protected final SocketChannel socketChannel;
    protected SelectionKey selectionKey;
    protected ByteBuffer readBuffer;
    protected ByteBuffer writeBuffer;

    private volatile LinkedList<ByteBuffer> bufferQueue = new LinkedList<>();

    private AtomicBoolean writeFlag = new AtomicBoolean(false);


    public NioHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.readBuffer = ByteBuffer.allocateDirect(MAX_READ);
        this.selector = selector;
        this.selector.wakeup();
        this.socketChannel = socketChannel;
        //设置为非阻塞状态
        this.socketChannel.configureBlocking(false);
        //注册写事件
        //TODO 使用select(timeout),register方法阻塞几秒，如何解决？
        //TODO 使用select(),register方法阻塞死掉，如何解决？
        //http://tool.oschina.net/apidocs/apidoc?api=jdk-zh
        //如果一个selection thread已经在select方法上等待ing,那么这个时候如果有另一条线程调用channal.register方法的话,那么它将被blocking.
        //http://blog.csdn.net/chenxuegui1234/article/details/17766813
        long start = System.currentTimeMillis();
        this.selectionKey = this.socketChannel.register(selector, SelectionKey.OP_READ);
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " register selector time:" + (end - start) / 1000 + "s");
        this.selectionKey.attach(this);
        this.onConnection(this.socketChannel);
    }

    public void onConnection(SocketChannel socketChannel) throws IOException {
        System.out.println(Thread.currentThread().getName() + " connection has to accept,socket channel is " + socketChannel);
    }

    public void doReadData() throws IOException {
        throw new IOException("If don`t implement NioHandler.doReadData() method,you can`t read socket data!");
    }

    //TODO 写出数据大，使用bufferpool
    public void doWriteData() throws IOException {
        while (!writeFlag.compareAndSet(false, true)) {
            //until the release
            System.out.println("doWriteData:until the release");
        }
        try {
            writeToChannel(writeBuffer);
        } finally {
            writeFlag.lazySet(false);
        }
    }


    public void writeData(byte[] data) throws IOException {
        while (!writeFlag.compareAndSet(false, true)) {
            //until the release
            System.out.println("writeData:until the release");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            if (writeBuffer == null && bufferQueue.isEmpty()) {
                System.out.println("writeBuffer is null,bufferQueue is empty,and write buffer");
                writeToChannel(buffer);
            } else {
                System.out.println("bufferQueue add buffer,and write writeBuffer");
                bufferQueue.add(buffer);
                writeToChannel(writeBuffer);
            }
            writeToChannel(buffer);
        } finally {
            writeFlag.lazySet(false);
        }
    }

    //TODO 写出数据大，使用bufferpool
    public void writeToChannel(ByteBuffer byteBuffer) throws IOException {
        int writeNum = this.socketChannel.write(byteBuffer);
        System.out.println("write num:" + writeNum);
        if (byteBuffer.hasRemaining()) {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            this.selector.wakeup();
            if (writeBuffer != byteBuffer) {
                writeBuffer = byteBuffer;
            }
        } else {
            System.out.println("finish writing byteBuffer");
            if (bufferQueue.isEmpty()) {
                System.out.println("bufferQueue is empty");
                selectionKey.interestOps(SelectionKey.OP_READ);
                this.selector.wakeup();
            } else {
                System.out.println("write bufferQueue");
                ByteBuffer curByteBuffer = bufferQueue.removeFirst();
                writeToChannel(curByteBuffer);
            }
        }
    }

}
