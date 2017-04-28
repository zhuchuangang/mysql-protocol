package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/4.
 */
public class ColumnsNumberPacket extends MySQLPacket {

    public long columnsNumber;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        packetLength = message.readUB3();
        packetSequenceId = message.read();
        columnsNumber = message.readLength();
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
        return "response to a COM_QUERY packet";
    }

    @Override
    public String toString() {
        return "ColumnsNumberPacket{" +
                "  columnsNumber=" + columnsNumber +
                ", packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                "}\n";
    }
}
