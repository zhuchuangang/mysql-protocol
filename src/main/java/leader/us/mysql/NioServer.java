package leader.us.mysql;

import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.net.NioAcceptor;
import leader.us.mysql.net.NioConnector;
import leader.us.mysql.net.NioReactor;
import leader.us.mysql.net.SystemConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zcg on 2017/5/2.
 */
public class NioServer {
    public static void main(String[] args) {
        SystemConfig config = new SystemConfig("127.0.0.1", 3308, "uaa", "root", "123456");
        ExecutorService executorService = Executors.newCachedThreadPool();
        DirectByteBufferPool bufferPool = DirectByteBufferPool.getInstance();
        try {
            NioReactor[] reactors = new NioReactor[Runtime.getRuntime().availableProcessors()];
            for (int i = 0; i < reactors.length; i++) {
                reactors[i] = new NioReactor(executorService, bufferPool, "nio-reactor-" + i);
                reactors[i].start();
            }
            NioConnector connector = new NioConnector(config, reactors, bufferPool);
            connector.start();
            NioAcceptor acceptor = new NioAcceptor(3306, reactors);
            acceptor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
