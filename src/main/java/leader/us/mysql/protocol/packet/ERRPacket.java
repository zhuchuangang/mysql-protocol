package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * int<1>	header	[ff] header of the ERR packet
 * int<2>	error_code	error-code
 * if capabilities & CLIENT_PROTOCOL_41 {
 * string[1]	sql_state_marker	# marker of the SQL State
 * string[5]	sql_state	SQL State
 * }
 * string<EOF>	error_message	human readable error message
 */
public class ERRPacket extends MySQLPacket {

    // int<1>	header	[ff] header of the ERR packet
    public int header;
    // int<2>	error_code	error-code
    public int errorCode;
    // if capabilities & CLIENT_PROTOCOL_41 {
//  string[1]	sql_state_marker	# marker of the SQL State
//  string[5]	sql_state	SQL State
// }
    public byte sqlStateMarker;

    public String sqlState;
    // string<EOF>	error_message	human readable error message
    public String errorMessage;

    public int capabilities;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        this.packetLength = message.readUB3();
        this.packetSequenceId = message.read();
        this.header = message.read();
        this.errorCode = message.readUB2();
        if ((capabilities & CapabilityFlags.CLIENT_PROTOCOL_41.getCode()) > 0) {
            sqlStateMarker = message.read();
            sqlState = new String(message.readBytes(5));
        }
        errorMessage = message.readString();
    }

    @Override
    public void write(ByteBuffer buffer) {
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetSequenceId);
        buffer.put((byte) 0xff);
        BufferUtil.writeUB2(buffer, errorCode);
        if ((capabilities & CapabilityFlags.CLIENT_PROTOCOL_41.getCode()) > 0) {
            buffer.put((byte) '#');
            buffer.put(sqlState.getBytes());
        }
        if (errorMessage != null) {
            buffer.put(errorMessage.getBytes());
        }
    }

    @Override
    public int calcPacketSize() {
        int size = 9;
        if (errorMessage != null) {
            size += errorMessage.length();
        }
        return size;
    }

    @Override
    public String getPacketInfo() {
        return "MySQL Error Packet";
    }

    @Override
    public String toString() {
        return "ERRPacket{" +
                "packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", header=" + header +
                ", errorCode=" + errorCode +
                ", sqlStateMarker=" + sqlStateMarker +
                ", sqlState=" + sqlState +
                ", errorMessage='" + errorMessage + '\'' +
                ", capabilities=" + capabilities +
                "}\n";
    }
}
