package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.packet.MySQLPacket;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/27.
 */
public interface MultiResultSetState {
    MySQLPacket read(ByteBuffer buffer);
}
