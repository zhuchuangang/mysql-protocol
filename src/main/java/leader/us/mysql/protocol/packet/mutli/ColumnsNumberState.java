package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.packet.ColumnsNumberPacket;
import leader.us.mysql.protocol.packet.MySQLPacket;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/27.
 */
public class ColumnsNumberState implements MultiResultSetState {

    private MultiResultSetContext context;

    public ColumnsNumberState(MultiResultSetContext context) {
        this.context = context;
    }

    @Override
    public MySQLPacket read(ByteBuffer buffer) {
        ColumnsNumberPacket cnp = new ColumnsNumberPacket();
        cnp.read(buffer);
        context.setColumnsNumber(cnp.columnsNumber);
        context.setState(context.fieldState);
        return cnp;
    }
}
