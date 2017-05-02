package leader.us.mysql.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * reactor作为IO线程，业务处理的handler线程在线程池中执行
 */
public class NioReactor extends Thread {
    //所有的reactor的线程池为公用
    private ExecutorService executorService;
    //每个reactor都有各自的selector
    private Selector selector;
    //注册队列
    private ConcurrentLinkedQueue<SocketChannel> registerQueue;

    public NioReactor(ExecutorService executorService, int index) throws IOException {
        this.executorService = executorService;
        this.selector = Selector.open();
        this.registerQueue = new ConcurrentLinkedQueue();
        setName("nio-reactor-" + index);
        System.out.println(getName() + " create reactor thread:" + getName());
    }

    public void postRegister(SocketChannel channel) {
        registerQueue.offer(channel);
        selector.wakeup();
        System.out.println(getName() + " add the channel to register queue,and wakeup selector");
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
            //System.out.println(getName() + " register queue is empty");
            return;
        }
        SocketChannel c = null;
        while ((c = registerQueue.poll()) != null) {
            System.out.println(getName() + " register queue poll " + c);
            try {
                new TelnetHandler(selector,c);
                System.out.println(getName() + " create nioHandler,and register channel to listen for read event");
            } catch (Exception e) {

            }
        }
    }
}
