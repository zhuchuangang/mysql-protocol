package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * lenenc_str     catalog
 * lenenc_str     schema
 * lenenc_str     table
 * lenenc_str     org_table
 * lenenc_str     name
 * lenenc_str     org_name
 * lenenc_int     length of fixed-length fields [0c]
 * 2              character set
 * 4              column length
 * 1              type
 * 2              flags
 * 1              decimals
 * 2              filler [00] [00]
 * if command was COM_FIELD_LIST {
 * lenenc_int     length of default-values
 * string[$len]   default values
 * }
 */
public class ColumnPacket extends MySQLPacket {
    //    lenenc_str     catalog
    public String catalog;
    //    lenenc_str     schema
    public String schema;
    //    lenenc_str     table
    public String table;
    //    lenenc_str     org_table
    public String orgTable;
    //    lenenc_str     name
    public String name;
    //    lenenc_str     org_name
    public String orgName;

    //    lenenc_int     length of fixed-length fields [0c]
    //            2              character set
    public int characterSet;
    //4              column length
    public int columnLength;
    //1              type
    public int type;
    //2              flags
    public int flags;
    //1              decimals
    public byte decimals;
    //2              filler [00] [00]
    //if command was COM_FIELD_LIST {
    //      lenenc_int     length of default-values
    //      string[$len]   default values
    //}
//    public long defaultValuesLength;
//
//    public String defualtValues;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        packetLength = message.readUB3();
        packetSequenceId = message.read();
        catalog = message.readStringWithLength();
        schema = message.readStringWithLength();
        table = message.readStringWithLength();
        orgTable = message.readStringWithLength();
        name = message.readStringWithLength();
        orgName = message.readStringWithLength();
        message.move(1);
        characterSet = message.readUB2();
        columnLength = message.readUB4();
        type = (message.read() & 0xff);
        flags = message.readUB2();
        decimals = message.read();
        message.move(2);
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
        return "MySQL Column Packet";
    }

    @Override
    public String toString() {
        return "ColumnPacket{" +
                "  packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", catalog='" + catalog + '\'' +
                ", schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                ", orgTable='" + orgTable + '\'' +
                ", name='" + name + '\'' +
                ", orgName='" + orgName + '\'' +
                ", characterSet=" + characterSet +
                ", columnLength=" + columnLength +
                ", type=" + type +
                ", flags=" + flags +
                ", decimals=" + decimals +
                "}\r\n";
    }
}
