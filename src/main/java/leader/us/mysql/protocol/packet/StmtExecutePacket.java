package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CommandTypes;
import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;


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

    public int statementId;
    public int flags;
    public int paramCount;
    public boolean sendType;
    public List<String> params;


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
        buffer.put((byte) (sendType ? 1 : 0));
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
            if (sendType) {
                size += paramCount * 2;
            }
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
}
