package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.constants.ClientCapabilityFlags;
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
        ep.capabilities = ClientCapabilityFlags.getClientCapabilities();
        ep.read(buffer);
        if (context.getReadRow()) {
            context.setState(context.rowState);
        } else {
            ByteBuffer bb = buffer.slice();
            if (bb.limit() <= 4) {
                context.setState(null);
            } else {
                byte head = bb.get(4);
                if (head == (byte) 0xff) {
                    context.setState(context.errorState);
                } else if (head == (byte) 0x00) {
                    context.setState(context.okState);
                } else {
                    context.setState(context.columnsNumberState);
                }
            }
        }
        return ep;
    }
}
