package leader.us.mysql.net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

/**
 * acceptor服务启动serverSocketChannel,并接受客户端连接,不使用selector绑定OP_ACCEPT事件
 */
public class NioAcceptor extends Thread {

    private static Logger logger = LogManager.getLogger(NioAcceptor.class);
    private ServerSocketChannel serverSocketChannel;
    private NioReactor[] reactors;

    public NioAcceptor(int port, NioReactor[] reactors) throws IOException {
        this.reactors = reactors;
        serverSocketChannel = ServerSocketChannel.open();
        //serverSocketChannel设置为阻塞状态，接受客户端请求
        serverSocketChannel.configureBlocking(true);
        SocketAddress address = new InetSocketAddress(port);
        serverSocketChannel.bind(address);
        setName("nio-acceptor-0");
        logger.info("create acceptor thread:{},server socket start on {}", getName(), address);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                //直接使用accept()方法,因为通道是阻塞的，所有accept()方法也是阻塞的
                SocketChannel channel = serverSocketChannel.accept();
                logger.info("{} thread accept {}", getName(), channel);
                //随机生成reactors的下标
                int index = ThreadLocalRandom.current().nextInt(reactors.length);
                logger.info("{} thread choose no.{} reactor register channel", getName(), index);
                FrontendConnection connection = new FrontendConnection(channel);
                //调用reactor的registerClient方法，注册客户端连接
                reactors[index].postRegister(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
