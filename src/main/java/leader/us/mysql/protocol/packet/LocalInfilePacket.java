package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/5/15.
 */
public class LocalInfilePacket extends MySQLPacket {

    public int header;
    public String filename;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage m = new MySQLMessage(buffer);
        packetLength=m.readUB3();
        packetSequenceId=m.read();
        this.header=m.read();
        this.filename=m.readString();
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
        return null;
    }
}
