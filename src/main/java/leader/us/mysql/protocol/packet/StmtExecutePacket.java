package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CommandTypes;
import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;
import java.util.*;


/**
 * int<1> 0x17 : COM_STMT_EXECUTE header
 * int<4> statement id
 * int<1> flags:
 * int<4> Iteration count (always 1)
 * if (param_count > 0)
 * byte<(param_count + 7)/8> null bitmap
 * byte<1>: send type to server (0 / 1)
 * if (send type to server)
 * for each parameter :
 * byte<1>: field type
 * byte<1>: parameter flag
 * for each parameter (i.e param_count times)
 * byte<n> binary parameter value
 */
public class StmtExecutePacket extends MySQLPacket {

    public int header;
    public int statementId;
    public int flags;
    public int paramCount;
    public byte sendType;
    public byte[] nullBitmap;
    public List<Map> fieldTypes;
    public List<String> params;


    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage m = new MySQLMessage(buffer);
        this.packetLength = m.readUB3();
        this.packetSequenceId = m.read();
        this.header = m.read();
        this.statementId = m.readUB4();
        this.flags = m.read();
        m.move(4);
        this.sendType= m.read();
        if (paramCount > 0) {
            int bitmapSize = (paramCount + 7) / 8;
            nullBitmap = m.readBytes(bitmapSize);
            fieldTypes=new ArrayList<>();
            for (int i = 0; i < paramCount; i++) {
                byte fieldType = m.read();
                byte parameterFlag = m.read();
                Map t = new HashMap();
                t.put(fieldType, parameterFlag);
                fieldTypes.add(t);
            }
            params = new ArrayList<>();
            for (int i = 0; i < paramCount; i++) {
                params.add(m.readStringWithLength());
            }
        }
    }

    @Override
    public void write(ByteBuffer buffer) {
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetSequenceId);
        buffer.put(CommandTypes.COM_STMT_EXECUTE);
        BufferUtil.writeInt(buffer, statementId);
        buffer.put((byte) flags);
        BufferUtil.writeUB4(buffer, 1);
        if (paramCount > 0) {
            int size = (paramCount + 7) / 8;
            for (int i = 0; i < size; i++) {
                buffer.put((byte) 0x0);
            }
        }
        for (int i = 0; i < paramCount; i++) {
            BufferUtil.writeWithLength(buffer, params.get(i).getBytes());
        }
    }


    /**
     * int<1> 0x17 : COM_STMT_EXECUTE header
     * int<4> statement id
     * int<1> flags:
     * int<4> Iteration count (always 1)
     * if (param_count > 0)
     * byte<(param_count + 7)/8> null bitmap
     * byte<1>: send type to server (0 / 1)
     * if (send type to server)
     * for each parameter :
     * byte<1>: field type
     * byte<1>: parameter flag
     * for each parameter (i.e param_count times)
     * byte<n> binary parameter value
     */
    @Override
    public int calcPacketSize() {
        int size = 11;
        if (paramCount > 0) {
            size += (paramCount + 7) / 8;
            size += paramCount * 2;
            for (String p : params) {
                size += BufferUtil.getLength(p.getBytes());
            }
        }
        return size;
    }

    @Override
    public String getPacketInfo() {
        return null;
    }

    @Override
    public String toString() {
        return "StmtExecutePacket{" +
                "packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", header=" + header +
                ", statementId=" + statementId +
                ", flags=" + flags +
                ", paramCount=" + paramCount +
                ", sendType=" + sendType +
                ", nullBitmap=" + Arrays.toString(nullBitmap) +
                ", fieldTypes=" + fieldTypes +
                ", params=" + params +
                '}';
    }
}
