package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * int<1> 0x00 COM_STMT_PREPARE_OK header
 * int<4> statement id
 * int<2> number of columns
 * int<2> number of parameters
 * string<1> -not used-
 * int<2> number of warnings
 */
public class StmtPrepareOKPacket extends MySQLPacket {

    public int header;

    public int statementId;

    public int columnsNumber;

    public int parametersNumber;

    public int warnings;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage m = new MySQLMessage(buffer);
        packetLength = m.readUB3();
        packetSequenceId = m.read();
        header = m.read();
        statementId = m.readUB4();
        columnsNumber = m.readUB2();
        parametersNumber = m.readUB2();
        m.move(1);
        warnings = m.readUB2();
    }

    @Override
    public void write(ByteBuffer buffer) {
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetSequenceId);
        buffer.put((byte) 0x00);
        BufferUtil.writeUB4(buffer, statementId);
        BufferUtil.writeUB2(buffer, columnsNumber);
        BufferUtil.writeUB2(buffer, parametersNumber);
        buffer.put((byte) 0x00);
        BufferUtil.writeUB2(buffer, warnings);
    }

    @Override
    public int calcPacketSize() {
        return 14;
    }

    @Override
    public String getPacketInfo() {
        return "Mysql Prepared Statement OK Packet";
    }

    @Override
    public String toString() {
        return "StmtPrepareOKPacket{" +
                "packetLength=" + packetLength +
                ", header=" + header +
                ", packetSequenceId=" + packetSequenceId +
                ", statementId=" + statementId +
                ", columnsNumber=" + columnsNumber +
                ", parametersNumber=" + parametersNumber +
                ", warnings=" + warnings +
                '}';
    }
}
