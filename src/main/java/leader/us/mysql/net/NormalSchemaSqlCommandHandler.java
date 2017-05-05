package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.protocol.packet.CommandPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/5/4.
 */
public class NormalSchemaSqlCommandHandler implements SqlCommandHandler {

    private static Logger logger = LogManager.getLogger(NormalSchemaSqlCommandHandler.class);

    @Override
    public Chunk response(Chunk chunk, SocketChannel socketChannel, FrontendHandler handler) {
        CommandPacket cp = new CommandPacket();
        cp.read(chunk.getBuffer());
        chunk.getBuffer().flip();
        logger.debug(cp);

        BackendConnectionPool connectionPool = BackendConnectionPool.getInstance();
        BackendConnection connection = connectionPool.connection();
        if (connection != null) {
            BackendHandler backendHandler = connection.getBackendHandler();
            backendHandler.setFrontendHandler(handler);
            try {
                backendHandler.writeData(chunk);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
