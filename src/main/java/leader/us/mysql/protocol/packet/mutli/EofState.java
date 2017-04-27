package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.packet.EOFPacket;
import leader.us.mysql.protocol.packet.MySQLPacket;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/27.
 */
public class EofState implements MultiResultSetState {
    private MultiResultSetContext context;

    public EofState(MultiResultSetContext context) {
        this.context = context;
    }

    @Override
    public MySQLPacket read(ByteBuffer buffer) {
        EOFPacket ep = new EOFPacket();
        ep.read(buffer);

        return ep;
    }
}
