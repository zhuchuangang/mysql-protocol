package leader.us.mysql.protocol.packet.passthou;

import leader.us.mysql.protocol.constants.ClientCapabilityFlags;
import leader.us.mysql.protocol.packet.ERRPacket;
import leader.us.mysql.protocol.packet.MySQLPacket;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/27.
 */
public class ErrorState implements PacketState {

    private ServerResponseContext context;

    public ErrorState(ServerResponseContext context) {
        this.context = context;
    }

    @Override
    public MySQLPacket read(ByteBuffer buffer) {
        ERRPacket ep = new ERRPacket();
        ep.capabilities = ClientCapabilityFlags.getClientCapabilities();
        ep.read(buffer);
        context.setState(null);
        return ep;
    }
}
