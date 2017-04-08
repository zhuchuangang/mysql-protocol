package leader.us.mysql.protocol.packet;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/4/2.
 */
public abstract class MySQLPacket {

    public final int HEADER_SIZE = 4;
    public final int MAX_PACKET_SIZE = 16 * 1024 * 1024; // Data between client and server is exchanged in packets of max 16MByte size.
    //   //https://dev.mysql.com/doc/internals/en/mysql-packet.html
    public int packetLength;    // 3字节
    public byte packetSequenceId;    // 1字节

    public abstract int calcPacketSize();

    public abstract String getPacketInfo();

    public void read(ByteBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    public void write(ByteBuffer buffer) {
        throw new UnsupportedOperationException();
    }
}