package leader.us.mysql.protocol.packet.passthou;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zcg on 2017/4/27.
 */
public class ServerResponseContext extends PacketState {


    final ColumnsCountState columnsCountState;
    final EofState eofState;
    final FieldState fieldState;
    final RowState rowState;
    final OkState okState;
    final ErrorState errorState;

    private DirectByteBufferPool bufferPool;

    private PacketState state;
    private long columnsNumber = 0;
    private boolean readRow = false;


    private ByteBuffer header = ByteBuffer.allocate(12);
    private MySQLMessage message = new MySQLMessage(header);
    private List<Chunk> chunks = new ArrayList<>();

    public ServerResponseContext(DirectByteBufferPool bufferPool) {
        columnsCountState = new ColumnsCountState(this);
        eofState = new EofState(this);
        fieldState = new FieldState(this);
        rowState = new RowState(this);
        okState = new OkState(this);
        errorState = new ErrorState(this);
        this.bufferPool = bufferPool;
    }


    public void read(SocketChannel channel) throws IOException {
        if (state == null) {
            header.limit(5);
            int readNum = channel.read(header);
            if (checkReadNum(readNum, channel)) {
                return;
            }
            header.flip();
            int packetLength = message.readUB3();
            byte packetType = header.get(4);

            //OK packet  0x00
            //LOCAL_INFILE packet 0xfb
            //EOF packet  0xfe
            //ERROR packet  0xff
            if ((packetType == 0x00 && packetLength > 7) ||
                    (packetType == 0xfe && packetLength == 5) ||
                    packetType == 0xff ||
                    packetType == 0xfb) {
                state = okState;
            } else {
                state = columnsCountState;
            }
            while (state != null) {
                state.read(channel);
            }

        }
    }

    public PacketState getState() {
        return state;
    }

    public void setState(PacketState state) {
        this.state = state;
    }

    public long getColumnsNumber() {
        return columnsNumber;
    }

    public void setColumnsNumber(long columnsNumber) {
        this.columnsNumber = columnsNumber;
    }

    public boolean getReadRow() {
        return readRow;
    }

    public void setReadRow(boolean readRow) {
        this.readRow = readRow;
    }

    public ByteBuffer getHeader() {
        return header;
    }

    public MySQLMessage getMessage() {
        return message;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public DirectByteBufferPool getBufferPool() {
        return bufferPool;
    }
}
