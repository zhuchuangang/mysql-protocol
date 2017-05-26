package leader.us.mysql.protocol.packet.passthou;

import leader.us.mysql.protocol.constants.ClientCapabilityFlags;
import leader.us.mysql.protocol.packet.EOFPacket;
import leader.us.mysql.protocol.packet.MySQLPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/4/27.
 */
public class EofState extends PacketState {
    private ServerResponseContext context;

    public EofState(ServerResponseContext context) {
        this.context = context;
    }

//    @Override
//    public MySQLPacket read(ByteBuffer buffer) {
//        EOFPacket ep = new EOFPacket();
//        ep.capabilities = ClientCapabilityFlags.getClientCapabilities();
//        ep.read(buffer);
//        if (context.getReadRow()) {
//            context.setState(context.rowState);
//        } else {
//            ByteBuffer bb = buffer.slice();
//            if (bb.limit() <= 4) {
//                context.setState(null);
//            } else {
//                byte head = bb.get(4);
//                if (head == (byte) 0xff) {
//                    context.setState(context.errorState);
//                } else if (head == (byte) 0x00) {
//                    context.setState(context.okState);
//                } else {
//                    context.setState(context.columnsCountState);
//                }
//            }
//        }
//        return ep;
//    }

    @Override
    void read(SocketChannel channel) throws IOException {

    }
}
