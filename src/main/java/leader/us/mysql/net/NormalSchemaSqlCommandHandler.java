package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.protocol.packet.CommandPacket;
import leader.us.mysql.protocol.packet.StmtExecutePacket;
import leader.us.mysql.protocol.support.MySQLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * Created by zcg on 2017/5/4.
 */
public class NormalSchemaSqlCommandHandler implements SqlCommandHandler {

    private static Logger logger = LogManager.getLogger(NormalSchemaSqlCommandHandler.class);

    @Override
    public Chunk response(Chunk chunk, SocketChannel socketChannel, FrontendHandler handler) {
        byte packetType = chunk.getBuffer().get(4);
        //COM_STMT_PREPARE
        if (packetType == 0x16) {

        }
        //COM_STMT_EXECUTE
        if (packetType == 0x17) {
            MySQLMessage mm = new MySQLMessage(chunk.getBuffer());
            mm.move(5);
            int statementId = mm.readUB4();
            chunk.getBuffer().position(0);
            StmtExecutePacket sep = new StmtExecutePacket();
            Map<Integer, Integer> spm = handler.getSession().getStmtIdParamCount();
            sep.paramCount = spm.get(statementId);
            sep.read(chunk.getBuffer());
            logger.debug(sep);
        } else {
            CommandPacket cp = new CommandPacket();
            cp.read(chunk.getBuffer());
            logger.debug(cp);
        }
        chunk.getBuffer().position(0);


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
