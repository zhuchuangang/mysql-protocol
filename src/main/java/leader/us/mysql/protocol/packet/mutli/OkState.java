package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.packet.MySQLPacket;
import leader.us.mysql.protocol.packet.OKPacket;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/27.
 */
public class OkState implements MultiResultSetState {

    private MultiResultSetContext context;

    public OkState(MultiResultSetContext context) {
        this.context = context;
    }

    @Override
    public MySQLPacket read(ByteBuffer buffer) {
        OKPacket op = new OKPacket();
        op.read(buffer);
        ByteBuffer bb = buffer.slice();
        if (bb.limit() <= 4) {
            context.setState(null);
        }
        return op;
    }
}
