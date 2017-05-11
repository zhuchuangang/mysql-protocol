package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.protocol.packet.CommandPacket;
import leader.us.mysql.protocol.packet.StmtExecutePacket;
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
        byte packetType = chunk.getBuffer().get(4);
        if (packetType == 0x16) {
            handler.getSession().setStmtPrepare(true);
        }
//        if (chunk.getBuffer().get(4) == 0x17) {
//            StmtExecutePacket sep = new StmtExecutePacket();
//            sep.read(chunk.getBuffer());
//            logger.debug(sep);
//        } else {
        CommandPacket cp = new CommandPacket();
        cp.read(chunk.getBuffer());
        logger.debug(cp);
//        }
        chunk.getBuffer().flip();

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
