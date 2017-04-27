package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.packet.MySQLPacket;

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
        return null;
    }
}
