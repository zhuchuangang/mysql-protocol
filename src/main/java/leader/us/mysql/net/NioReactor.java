package leader.us.mysql.net;

import leader.us.mysql.bufferpool.DirectByteBufferPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * reactor作为IO线程，业务处理的handler线程在线程池中执行
 */
public class NioReactor extends Thread {

    private static Logger logger = LogManager.getLogger(NioReactor.class);
    //所有的reactor的线程池为公用
    private ExecutorService executorService;

    private DirectByteBufferPool bufferPool;
    //每个reactor都有各自的selector
    private Selector selector;
    //注册队列
    private ConcurrentLinkedQueue<NioConnection> registerQueue;

    public NioReactor(ExecutorService executorService, DirectByteBufferPool bufferPool, String name) throws IOException {
        this.executorService = executorService;
        this.bufferPool = bufferPool;
        this.selector = Selector.open();
        this.registerQueue = new ConcurrentLinkedQueue();
        setName(name);
        logger.info("{} create reactor thread:{}", getName(), getName());
    }

    public void postRegister(NioConnection connection) {
        registerQueue.offer(connection);
        selector.wakeup();
        logger.info("{} add the channel to register queue,and wakeup selector", getName());
    }


    @Override
    public void run() {
        Set<SelectionKey> keys;
        int selectNum = 0;
        while (!Thread.interrupted()) {
            try {
                selectNum = selector.select(400 / (selectNum + 1));
                //System.out.println(getName() + " there is " + selectNum + " ready event");
                if (selectNum == 0) {
                    register();
                    continue;
                }
                keys = selector.selectedKeys();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                NioHandler handler = (NioHandler) key.attachment();
                if (handler != null) {
                    executorService.execute(handler);
                }
                iterator.remove();
            }
            keys.clear();
        }
    }

    private void register() {
        if (registerQueue.isEmpty()) {
            return;
        }
        NioConnection c = null;
        while ((c = registerQueue.poll()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} register queue poll {}", getName(), c);
            }
            try {
                if (c instanceof FrontendConnection) {
                    new FrontendHandler(selector, (FrontendConnection) c, bufferPool);
                } else if (c instanceof BackendConnection) {
                    new BackendHandler(selector, (BackendConnection) c, bufferPool);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("{} create nioHandler,and register channel to listen for read event", getName(), c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
