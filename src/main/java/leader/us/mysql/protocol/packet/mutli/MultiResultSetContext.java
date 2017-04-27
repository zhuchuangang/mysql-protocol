package leader.us.mysql.protocol.packet.mutli;

import leader.us.mysql.protocol.packet.MySQLPacket;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/27.
 */
public class MultiResultSetContext implements MultiResultSetState {

    final ColumnsNumberState columnsNumberState;
    final EofState eofState;
    final FieldState fieldState;
    final RowState rowState;
    final OkState okState;
    final ErrorState errorState;

    private MultiResultSetState state;
    private long columnsNumber = 0;
    private boolean readRow = false;

    public MultiResultSetContext() {
        columnsNumberState = new ColumnsNumberState(this);
        eofState = new EofState(this);
        fieldState = new FieldState(this);
        rowState = new RowState(this);
        okState = new OkState(this);
        errorState = new ErrorState(this);
    }

    @Override
    public MySQLPacket read(ByteBuffer buffer) {
        if (state == null) {
            if (buffer.limit() < 5) {
                return null;
            }
            byte head = buffer.get(4);
            switch (head) {
                case 0x00:
                    state = okState;
                    break;
                case (byte) 0xfe:
                    state = eofState;
                    break;
                case (byte) 0xff:
                    state = errorState;
                    break;
                default:
                    state = columnsNumberState;
            }
        }
        return state.read(buffer);
    }

    public MultiResultSetState getState() {
        return state;
    }

    public void setState(MultiResultSetState state) {
        this.state = state;
    }

    public long getColumnsNumber() {
        return columnsNumber;
    }

    public void setColumnsNumber(long columnsNumber) {
        this.columnsNumber = columnsNumber;
    }

    public boolean isReadRow() {
        return readRow;
    }

    public void setReadRow(boolean readRow) {
        this.readRow = readRow;
    }
}
