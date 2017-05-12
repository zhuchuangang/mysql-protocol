package leader.us.mysql.net;

import leader.us.mysql.protocol.packet.*;
import leader.us.mysql.protocol.support.MySQLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/5/11.
 */
public class MysqlResponseHandler {

    private static Logger logger = LogManager.getLogger(MysqlResponseHandler.class);

    public static void dump(ByteBuffer buffer, FrontendHandler frontendHandler) {
        MySQLMessage m = new MySQLMessage(buffer);
        int packetLength = m.readUB3();
        buffer.position(0);
        int packetType = buffer.get(4);
        //COM_STMT_PREPARE response
        if (packetType == 0x00 && packetLength == 12) {
            StmtPreparePacket sp = new StmtPreparePacket();
            sp.read(buffer);
            frontendHandler.getSession().getStmtIdParamCount().put(sp.stmtPrepareOKPacket.statementId, sp.stmtPrepareOKPacket.parametersNumber);
            System.out.println(sp);
        } else {
            switch (packetType) {
                //OK packet
                case 0x00:
                    OKPacket op = new OKPacket();
                    op.read(buffer);
                    logger.debug(op);
                    break;
                //LOCAL_INFILE packet
                case 0xfb:
                    break;
                //EOF packet
                case 0xfe:
                    EOFPacket ep = new EOFPacket();
                    ep.read(buffer);
                    logger.debug(ep);
                    break;
                //ERROR packet
                case 0xff:
                    ERRPacket errp = new ERRPacket();
                    errp.read(buffer);
                    break;
//                default:
//                    ResultSetPacket rsp = new ResultSetPacket();
//                    rsp.read(buffer);
//                    logger.debug(rsp);
            }
        }
        buffer.position(0);
    }
}
