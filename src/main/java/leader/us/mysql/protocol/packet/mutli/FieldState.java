package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.packet.ColumnPacket;
import leader.us.mysql.protocol.packet.MySQLPacket;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/27.
 */
public class FieldState implements MultiResultSetState {
    private MultiResultSetContext context;

    public FieldState(MultiResultSetContext context) {
        this.context = context;
    }

    @Override
    public MySQLPacket read(ByteBuffer buffer) {
        ColumnPacket cp = new ColumnPacket();
        cp.read(buffer);
        ByteBuffer temp=buffer.slice();
        MySQLMessage m = new MySQLMessage(temp);
        String def = m.readString();
        if (!"def".equals(def)) {
            context.setState(context.eofState);
        }
        return cp;
    }
}
