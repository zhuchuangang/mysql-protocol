package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.constants.StatusFlags;
import leader.us.mysql.protocol.support.BufferUtil;
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
            info = new String(message.readBytes(packetLength + 4 - buffer.position()));
        }
    }

    @Override
    public void write(ByteBuffer buffer) {
        this.packetLength = calcPacketSize();
        BufferUtil.writeUB3(buffer, this.packetLength);
        buffer.put(this.packetSequenceId);
        buffer.put((byte) 0x00);
        BufferUtil.writeLength(buffer, this.affectedRows);
        BufferUtil.writeLength(buffer, this.lastInsertId);
        if ((capabilities & CapabilityFlags.CLIENT_PROTOCOL_41.getCode()) != 0) {
            BufferUtil.writeUB2(buffer, this.statusFlags);
            BufferUtil.writeUB2(buffer, this.warnings);
        } else if ((capabilities & CapabilityFlags.CLIENT_TRANSACTIONS.getCode()) != 0) {
            BufferUtil.writeUB2(buffer, this.statusFlags);
        }
        if ((capabilities & CapabilityFlags.CLIENT_SESSION_TRACK.getCode()) != 0) {
            BufferUtil.writeWithLength(buffer, this.info.getBytes());
            if ((statusFlags & StatusFlags.SERVER_SESSION_STATE_CHANGED.getCode()) != 0) {
                BufferUtil.writeWithLength(buffer, this.sessionStateChanges.getBytes());
            }
        } else {
            if (info!=null) {
                buffer.put(info.getBytes());
            }
        }
    }

    @Override
    public int calcPacketSize() {
        int size = 1
                + BufferUtil.getLength(this.affectedRows)
                + BufferUtil.getLength(this.lastInsertId);
        if ((capabilities & CapabilityFlags.CLIENT_PROTOCOL_41.getCode()) != 0) {
            size += 4;
        } else if ((capabilities & CapabilityFlags.CLIENT_TRANSACTIONS.getCode()) != 0) {
            size += 2;
        }
        if ((capabilities & CapabilityFlags.CLIENT_SESSION_TRACK.getCode()) != 0) {
            size += BufferUtil.getLength(this.info.getBytes());
            if ((statusFlags & StatusFlags.SERVER_SESSION_STATE_CHANGED.getCode()) != 0) {
                size += BufferUtil.getLength(this.sessionStateChanges.getBytes());
            }
        } else {
            if (info!=null) {
                size += this.info.getBytes().length;
            }
        }

        return size;
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
