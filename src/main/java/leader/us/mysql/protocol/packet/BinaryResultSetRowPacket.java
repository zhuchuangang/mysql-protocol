package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.MySQLTypes;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zcg on 2017/4/4.
 */
public class BinaryResultSetRowPacket extends MySQLPacket {

    public List values = new ArrayList();

    public List<ColumnPacket> columns;

    public int header;

    public byte[] nullBitmap;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        packetLength = message.readUB3();
        packetSequenceId = message.read();
        header = message.read();
        if (columns != null && columns.size() > 0) {
            int size = (columns.size() + 9) / 8;
            nullBitmap = message.readBytes(size);
        }
        if (columns != null && columns.size() > 0) {
            for (int i = 0; i < columns.size(); i++) {
                if ((nullBitmap[(i + 2) / 8] & (1 << ((i + 2) % 8))) > 0) {
                    values.add("null");
                }else {
                    ColumnPacket cp = columns.get(i);
                    values.add(MySQLTypes.data(cp.type, message));
                }
            }
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
        return "MySQL Binary ResultSet Row Packet";
    }

    @Override
    public String toString() {
        return "BinaryResultSetRowPacket{" +
                "values=" + values +
                ", packetLength=" + packetLength +
                ", header=" + header +
                ", packetSequenceId=" + packetSequenceId +
                '}';
    }
}
