package leader.us.mysql.protocol.packet.passthou;

import leader.us.mysql.protocol.packet.ColumnPacket;
import leader.us.mysql.protocol.packet.MySQLPacket;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/4/27.
 */
public class FieldState extends PacketState {
    private ServerResponseContext context;

    public FieldState(ServerResponseContext context) {
        this.context = context;
    }

    @Override
    void read(SocketChannel channel) throws IOException {

    }

//    @Override
//    public MySQLPacket read(ByteBuffer buffer) {
//        ColumnPacket cp = new ColumnPacket();
//        cp.read(buffer);
//        ByteBuffer temp = buffer.slice();
//        MySQLMessage m = new MySQLMessage(temp);
//        m.move(4);
//        int position = temp.position();
//        byte head = m.read();
//        if (head == (byte) 0xfe) {
//            context.setReadRow(true);
//            context.setState(context.eofState);
//        }
//        return cp;
//    }
}
