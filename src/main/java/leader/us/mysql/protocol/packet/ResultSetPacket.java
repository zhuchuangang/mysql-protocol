package leader.us.mysql.protocol.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * https://dev.mysql.com/doc/internals/en/com-query-response.html#text-resultset-row
 */
public class ResultSetPacket extends MySQLPacket {

    public ColumnsNumberPacket columnsNumber;

    public List<ColumnPacket> columns = new ArrayList<>();

    public EOFPacket columnsEof;

    public List<ResultSetRowPacket> rows = new ArrayList<>();

    public EOFPacket rowsEof;

    public int capabilities;


    @Override
    public void read(ByteBuffer buffer) {
        columnsNumber = new ColumnsNumberPacket();
        columnsNumber.read(buffer);

        for (int i = 0; i < this.columnsNumber.columnsNumber; i++) {
            buffer = buffer.slice();
            ColumnPacket cp = new ColumnPacket();
            cp.read(buffer);
            columns.add(cp);
        }

        buffer = buffer.slice();
        if ((buffer.get(4) & 0xff) == 0xfe) {
            columnsEof = new EOFPacket();
            columnsEof.read(buffer);
        }
        for (; ; ) {
            buffer = buffer.slice();
            if ((buffer.get(4) & 0xff) == 0xfe) {
                rowsEof = new EOFPacket();
                rowsEof.read(buffer);
                break;
            } else {
                ResultSetRowPacket rsrp = new ResultSetRowPacket();
                rsrp.columnCount = columns.size();
                rsrp.read(buffer);
                rows.add(rsrp);
            }
        }


//        buffer = buffer.slice();
//        columnsEof = new EOFPacket();
//        columnsEof.read(buffer);
//
//        buffer = buffer.slice();
//        ResultSetRowPacket rsrp = new ResultSetRowPacket();
//        rsrp.columnCount = columns.size();
//        rsrp.read(buffer);
//        rows.add(rsrp);
//
//        buffer = buffer.slice();
//        rowsEof = new EOFPacket();
//        rowsEof.read(buffer);
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

    @Override
    public String toString() {
        return "ResultSetPacket{" +
                "\n  packetLength=" + packetLength +
                "\n, packetSequenceId=" + packetSequenceId +
                "\n, columnsNumber=" + columnsNumber +
                "\n, columns=" + columns +
                "\n, columnsEof=" + columnsEof +
                "\n, rows=" + rows +
                "\n, rowsEof=" + rowsEof +
                "\n, capabilities=" + capabilities +
                "\n}";
    }
}
