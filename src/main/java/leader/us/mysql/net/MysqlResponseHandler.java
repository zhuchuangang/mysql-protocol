package leader.us.mysql.net;

import leader.us.mysql.protocol.packet.StmtPreparePacket;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/5/11.
 */
public class MysqlResponseHandler {

    public static void dump(ByteBuffer buffer, FrontendHandler frontendHandler) {

        if (frontendHandler.getSession().getStmtPrepare()) {
            StmtPreparePacket sp = new StmtPreparePacket();
            sp.read(buffer);
            frontendHandler.getSession().getStmtIdParamCount().put(sp.stmtPrepareOKPacket.statementId, sp.stmtPrepareOKPacket.parametersNumber);
            buffer.position(0);
            frontendHandler.getSession().setStmtPrepare(false);
            System.out.println(sp);
        }
    }
}
