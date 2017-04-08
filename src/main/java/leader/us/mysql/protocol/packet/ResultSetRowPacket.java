package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zcg on 2017/4/4.
 */
public class ResultSetRowPacket extends MySQLPacket {

    public List values = new ArrayList();

    public int columnCount;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        packetLength = message.readUB3();
        packetSequenceId = message.read();
        for (int i = 0; i < columnCount; i++) {
            values.add(message.readStringWithLength());
        }
    }

    @Override
    public void write(ByteBuffer buffer) {
        super.write(buffer);
    }


    @Override
    public int calcPacketSize() {
        return 0;
    }

    @Override
    public String getPacketInfo() {
        return "MySQL ResultSet Row Packet";
    }

    @Override
    public String toString() {
        return "ResultSetRowPacket{" +
                " packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", values=" + values +
                "}\n";
    }
}
