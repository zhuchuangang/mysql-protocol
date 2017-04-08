package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

/**
 * 4              capabilities flags, CLIENT_PROTOCOL_41 always set
 * 4              max-packet size
 * 1              character set
 * string[23]     reserved (all [0])
 * string[NUL]    username
 * <p>
 * if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
 * lenenc-int     length of auth-response
 * string[n]      auth-response
 * <p>
 * } else if capabilities & CLIENT_SECURE_CONNECTION {
 * 1              length of auth-response
 * string[n]      auth-response
 * <p>
 * } else {
 * string[NUL]    auth-response
 * }
 * <p>
 * if capabilities & CLIENT_CONNECT_WITH_DB {
 * string[NUL]    database
 * }
 * <p>
 * if capabilities & CLIENT_PLUGIN_AUTH {
 * string[NUL]    auth plugin name
 * }
 * <p>
 * if capabilities & CLIENT_CONNECT_ATTRS {
 * lenenc-int     length of all key-values
 * lenenc-str     key
 * lenenc-str     value
 * if-more data in 'length of all key-values', more keys and value pairs
 * }
 */
public class AuthPacket extends MySQLPacket {

    private static final byte[] FILLER = new byte[23];

    /**
     * 能力标志位，4个字节  capabilities flags, CLIENT_PROTOCOL_41 always set
     */
    public int capabilityFlags;
    /**
     * 最大包大小  4个字节  max-packet size
     */
    public int maxPacket;
    /**
     * 字符集     1个字节   character set
     */
    public byte characterSet;
    /**
     * 保留位    string[23]   reserved (all [0])
     */
    public byte[] reserved;
    /**
     * 用户名    string[NUL]    username
     */
    public String username;

    /**
     * if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
     * lenenc-int     length of auth-response
     * string[n]      auth-response
     * } else if capabilities & CLIENT_SECURE_CONNECTION {
     * 1              length of auth-response
     * string[n]      auth-response
     * } else {
     * string[NUL]    auth-response
     * }
     * <p>
     * 如果capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA!=0,
     * 那么authResponseLength是一个length encoded integer,authResponse为长度为authResponseLength的字符串
     * 如果capabilities & CLIENT_SECURE_CONNECTION!=0,
     * 那么authResponseLength是一个字节的数,authResponse为长度为authResponseLength的字符串
     * 以上都不满足，
     * 那么authResponse为以null结尾的字符串
     */
    public byte[] password;

    public long authResponseLength;

    public String authResponse;


    /**
     * if capabilities & CLIENT_CONNECT_WITH_DB {
     * string[NUL]    database
     * }
     * <p>
     * 如果capabilities & CLIENT_CONNECT_WITH_DB!=0，那么database是以null结尾的字符串
     */
    public String database;

    /**
     * if capabilities & CLIENT_PLUGIN_AUTH {
     * string[NUL]    auth plugin name
     * }
     * <p>
     * 如果capabilities & CLIENT_PLUGIN_AUTH!=0,那么authPluginName是一个以null结尾的字符串
     */
    public String authPluginName;

    /**
     * if capabilities & CLIENT_CONNECT_ATTRS {
     * lenenc-int     length of all key-values
     * lenenc-str     key
     * lenenc-str     value
     * if-more data in 'length of all key-values', more keys and value pairs
     * }
     * <p>
     * 如果capabilities & CLIENT_CONNECT_ATTRS!=0,那么keyValuesLength是一个length encoded integer;
     * 可能会有多个键值对
     */
    public String keyValuesLength;

    public Map<String, String> values;

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        this.packetLength = message.readUB3();
        this.packetSequenceId = message.read();
        this.capabilityFlags = message.readUB4();
        this.characterSet = message.read();
        this.reserved = FILLER;
        this.username = message.readStringWithNull();
        if ((this.capabilityFlags & CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getCode()) != 0) {
            //this.authResponseLength = message.readLength();
            //this.authResponse = message.read(this.authResponseLength);
        } else if ((this.capabilityFlags & CapabilityFlags.CLIENT_SECURE_CONNECTION.getCode()) != 0) {

        } else {

        }
        if ((capabilityFlags & CapabilityFlags.CLIENT_CONNECT_WITH_DB.getCode()) != 0) {
            database = message.readStringWithNull();
        }
        if ((capabilityFlags & CapabilityFlags.CLIENT_PLUGIN_AUTH.getCode()) != 0) {
            authPluginName = message.readStringWithNull();
        }


    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.position(0);
        this.packetLength = calcPacketSize();
        BufferUtil.writeUB3(buffer, this.packetLength);
        buffer.put(this.packetSequenceId);
        BufferUtil.writeUB4(buffer, this.capabilityFlags);
        BufferUtil.writeInt(buffer, this.maxPacket);
        buffer.put(this.characterSet);
        this.reserved = FILLER;
        buffer.put(FILLER);
        if (this.username == null) {
            buffer.put((byte) 0);
        } else {
            BufferUtil.writeWithNull(buffer, this.username.getBytes());
        }
        if (this.password == null) {
            buffer.put((byte) 0);
        } else {
            BufferUtil.writeWithLength(buffer, this.password);
        }
        if (this.database == null) {
            buffer.put((byte) 0);
        } else {
            BufferUtil.writeWithNull(buffer, this.database.getBytes());
        }
        if (this.authPluginName == null) {
            buffer.put((byte) 0);
        } else {
            BufferUtil.writeWithNull(buffer, this.authPluginName.getBytes());
        }
        buffer.limit(buffer.position());
    }

    @Override
    public int calcPacketSize() {
        int size = 9 + 23;
        if (this.username != null) {
            size += this.username.length() + 1;
        } else {
            size += 1;
        }
        if (this.password != null) {
            size += BufferUtil.getLength(this.password);
        } else {
            size += 1;
        }
        if (this.database != null) {
            size += this.database.length() + 1;
        } else {
            size += 1;
        }
        if (this.authPluginName != null) {
            size += this.authPluginName.length() + 1;
        } else {
            size += 1;
        }
        return size;
    }

    @Override
    public String getPacketInfo() {
        return "MySQL Authentication Packet";
    }

    @Override
    public String toString() {
        return "AuthPacket{" +
                "packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", capabilityFlags=" + capabilityFlags +
                ", maxPacket=" + maxPacket +
                ", characterSet=" + characterSet +
                ", reserved=" + Arrays.toString(reserved) +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", authResponseLength=" + authResponseLength +
                ", authResponse='" + authResponse + '\'' +
                ", database='" + database + '\'' +
                ", authPluginName='" + authPluginName + '\'' +
                ", keyValuesLength='" + keyValuesLength + '\'' +
                ", values=" + values +
                '}';
    }
}
