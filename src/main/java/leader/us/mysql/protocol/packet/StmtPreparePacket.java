package leader.us.mysql.protocol.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * COM_STMT_PREPARE_OK
 * for each column:
 * column definition packet
 * if number of columns > 0 and !DEPRECATE_EOF eof_packet
 * for each parameter:
 * column definition packet
 * if number of parameters > 0 and !DEPRECATE_EOF eof_packet
 */
public class StmtPreparePacket extends MySQLPacket {

    public StmtPrepareOKPacket stmtPrepareOKPacket;
    public List<ColumnPacket> columns;
    public EOFPacket eofPacket1;
    public List<ColumnPacket> parameters;
    public EOFPacket eofPacket2;

    @Override
    public void read(ByteBuffer buffer) {
        stmtPrepareOKPacket = new StmtPrepareOKPacket();
        stmtPrepareOKPacket.read(buffer);
        buffer = buffer.slice();
        if (stmtPrepareOKPacket.parametersNumber > 0) {
            parameters = new ArrayList<ColumnPacket>();
            for (int i = 0; i < stmtPrepareOKPacket.parametersNumber; i++) {
                ColumnPacket cp = new ColumnPacket();
                cp.read(buffer);
                parameters.add(cp);
                buffer = buffer.slice();
            }
        }
        eofPacket1 = new EOFPacket();
        eofPacket1.read(buffer);
        buffer = buffer.slice();
        if (stmtPrepareOKPacket.columnsNumber > 0) {
            columns = new ArrayList<ColumnPacket>();
            for (int i = 0; i < stmtPrepareOKPacket.columnsNumber; i++) {
                ColumnPacket cp = new ColumnPacket();
                cp.read(buffer);
                columns.add(cp);
                buffer = buffer.slice();
            }
        }
        eofPacket2 = new EOFPacket();
        eofPacket2.read(buffer);

    }

    @Override
    public int calcPacketSize() {
        return 14;
    }

    @Override
    public String getPacketInfo() {
        return "Mysql Prepared Statement Packet";
    }

    @Override
    public String toString() {
        return "StmtPreparePacket{" +
                "\nstmtPrepareOKPacket=" + stmtPrepareOKPacket +
                ", \nparameters=" + parameters +
                ", \neofPacket1=" + eofPacket1 +
                ", \ncolumns=" + columns +
                ", \neofPacket2=" + eofPacket2 +
                '}';
    }
}
