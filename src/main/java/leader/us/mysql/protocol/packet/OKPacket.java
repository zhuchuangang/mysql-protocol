package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.constants.StatusFlags;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * int<1>	header	[00] or [fe] the OK packet header
 * int<lenenc>	affected_rows	affected rows
 * int<lenenc>	last_insert_id	last insert-id
 * if capabilities & CLIENT_PROTOCOL_41 {
 * int<2>	status_flags	Status Flags
 * int<2>	warnings	number of warnings
 * } elseif capabilities & CLIENT_TRANSACTIONS {
 * int<2>	status_flags	Status Flags
 * }
 * if capabilities & CLIENT_SESSION_TRACK {
 * string<lenenc>	info	human readable status information
 * if status_flags & SERVER_SESSION_STATE_CHANGED {
 * string<lenenc>	session_state_changes	session state info
 * }
 * } else {
 * string<EOF>	info	human readable status information
 * }
 */
public class OKPacket extends MySQLPacket {

    //  int<1>	header	[00] or [fe] the OK packet header
    public byte header;
    //  int<lenenc>	affected_rows	affected rows
    public long affectedRows;
    //  int<lenenc>	last_insert_id	last insert-id
    public long lastInsertId;
    //  if capabilities & CLIENT_PROTOCOL_41 {
//     int<2>	status_flags	Status Flags
//     int<2>	warnings	number of warnings
//    } elseif capabilities & CLIENT_TRANSACTIONS {
//     int<2>	status_flags	Status Flags
//    }
    public int statusFlags;
    public int warnings;
    //  if capabilities & CLIENT_SESSION_TRACK {
//           string<lenenc>	info	human readable status information
//        if status_flags & SERVER_SESSION_STATE_CHANGED {
//           string<lenenc>	session_state_changes	session state info
//        }
//    } else {
//           string<EOF>	info	human readable status information
//    }
    public String info;
    public String sessionStateChanges;

    public int capabilities;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        packetLength = message.readUB3();
        packetSequenceId = message.read();
        header = message.read();
        affectedRows = message.readLength();
        lastInsertId = message.readLength();
        if ((capabilities & CapabilityFlags.CLIENT_PROTOCOL_41.getCode()) > 0) {
            statusFlags = message.readUB2();
            warnings = message.readUB2();
        } else if ((capabilities & CapabilityFlags.CLIENT_TRANSACTIONS.getCode()) > 0) {
            statusFlags = message.readUB2();
        }
        if ((capabilities & CapabilityFlags.CLIENT_SESSION_TRACK.getCode()) > 0) {
            info = message.readStringWithLength();
            if ((statusFlags & StatusFlags.SERVER_SESSION_STATE_CHANGED.getCode()) > 0) {
                sessionStateChanges = message.readStringWithLength();
            }
        } else {

        }
    }

    @Override
    public void write(ByteBuffer buffer) {

    }

    @Override
    public int calcPacketSize() {
        return 0;
    }

    @Override
    public String getPacketInfo() {
        return "MySQL OK Packet";
    }

    @Override
    public String toString() {
        return "OKPacket{" +
                "packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", header=" + header +
                ", affectedRows=" + affectedRows +
                ", lastInsertId=" + lastInsertId +
                ", statusFlags=" + statusFlags +
                ", warnings=" + warnings +
                ", info='" + info + '\'' +
                ", sessionStateChanges='" + sessionStateChanges + '\'' +
                "}\n";
    }
}
