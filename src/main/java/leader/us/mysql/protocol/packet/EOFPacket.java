package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * int<1>	header	[fe] EOF header
 * if capabilities & CLIENT_PROTOCOL_41 {
 * int<2>	warnings	number of warnings
 * int<2>	status_flags	Status Flags
 * }
 */
public class EOFPacket extends MySQLPacket {

    public int header;

    public int warnings;

    public int statusFlags;

    public int capabilities;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        packetLength = message.readUB3();
        packetSequenceId = message.read();
        header = message.read() & 0xff;
        if ((capabilities & CapabilityFlags.CLIENT_PROTOCOL_41.getCode()) != 0) {
            warnings = message.readUB2();
            statusFlags = message.readUB2();
        } else {
            message.move(4);
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
        return "MySQL EOF Packet";
    }

    @Override
    public String toString() {
        return "EOFPacket{" +
                "  packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", header=" + header +
                ", warnings=" + warnings +
                ", statusFlags=" + statusFlags +
                ", capabilities=" + capabilities +
                "}\n";
    }
}
