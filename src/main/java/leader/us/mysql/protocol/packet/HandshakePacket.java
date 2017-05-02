package leader.us.mysql.protocol.packet;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.support.MySQLMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 1              [0a] protocol version
 * string[NUL]    server version
 * 4              connection id
 * string[8]      auth-plugin-data-part-1
 * 1              [00] filler
 * 2              capabilities flags (lower 2 bytes)
 * if more data in the packet:
 * 1              character set
 * 2              status flags
 * 2              capabilities flags (upper 2 bytes)
 * if capabilities & CLIENT_PLUGIN_AUTH {
 * 1              length of auth-plugin-data
 * } else {
 * 1              [00]
 * }
 * string[10]     reserved (all [00])
 * if capabilities & CLIENT_SECURE_CONNECTION {
 * string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
 * if capabilities & CLIENT_PLUGIN_AUTH {
 * string[NUL]    auth-plugin name
 * }
 */
public class HandshakePacket extends MySQLPacket {
    //https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake
    //  1 [0a] protocol version
    public int protocolVersion;
    //  string[NUL]    server version
    public String serverVersion;
    //  4 connection id
    public int connectionId;
    //  string[8]      auth-plugin-data-part-1
    public byte[] authPluginDataPart1;
    //  1              [00] filler
    public byte filler;
    //  2              capabilities flags (lower 2 bytes)
    public int capabilityLower;
    // if more data in the packet:
    //  1              character set
    public byte characterSet;
    //  2              status flags
    public int statusFlags;
    //  2              capabilities flags (upper 2 bytes)
    public int capabilityUpper;
    //  if capabilities & CLIENT_PLUGIN_AUTH {
    //  1    length of auth-plugin-data
    //      } else {
    //  1    [00]
    //      }
    public byte authPluginDataLen;
    // string[10]     reserved (all [00])
    public byte[] reserved;
    // if capabilities & CLIENT_SECURE_CONNECTION {
    //    string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
    //固定12位，后面一位是null
    public byte[] authPluginDataPart2;
    // if capabilities & CLIENT_PLUGIN_AUTH {
    // string[NUL]    auth-plugin name
    // }
    public String authPluginName;

    public int capabilities;

    public HandshakePacket() {
    }

    @Override
    public void read(ByteBuffer buffer) {
        MySQLMessage message = new MySQLMessage(buffer);
        packetLength = message.readUB3();
        packetSequenceId = message.read();
        protocolVersion = message.read();
        serverVersion = message.readStringWithNull();
        connectionId = message.readUB4();
        authPluginDataPart1 = message.readBytes(8);
        filler = message.read();
        capabilityLower = message.readUB2();
        characterSet = (byte) (message.read() & 0xff);
        statusFlags = message.readUB2();
        capabilityUpper = message.readUB2();
        capabilities = capabilityUpper << 16 | capabilityLower;
        authPluginDataLen = 0;
        if ((capabilities & CapabilityFlags.CLIENT_PLUGIN_AUTH.getCode()) > 0) {
            authPluginDataLen = message.read();
        } else {
            message.move(1);
        }
        reserved = message.readBytes(10);
        if ((capabilities & CapabilityFlags.CLIENT_SECURE_CONNECTION.getCode()) > 0) {
            //int len = Math.max(13, authPluginDataLen - 8);
            authPluginDataPart2 = message.readBytesWithNull();
        } else {
            message.move(13);
        }
        authPluginName = message.readStringWithNull();
    }

    @Override
    public void write(ByteBuffer buffer) {

    }

    @Override
    public int calcPacketSize() {
        return 0;
    }

    @Override
    public String getPacketInfo() {
        return "MySQL Handshake Packet";
    }

    @Override
    public String toString() {
        return "HandshakePacket{" +
                "packetLength=" + packetLength +
                ", packetSequenceId=" + packetSequenceId +
                ", protocolVersion=" + protocolVersion +
                ", serverVersion='" + serverVersion + '\'' +
                ", connectionId=" + connectionId +
                ", authPluginDataPart1=" + Arrays.toString(authPluginDataPart1) +
                ", filler=" + filler +
                ", capabilityLower=" + capabilityLower +
                ", characterSet=" + characterSet +
                ", statusFlags=" + statusFlags +
                ", capabilityUpper=" + capabilityUpper +
                ", authPluginDataLen=" + authPluginDataLen +
                ", reserved=" + Arrays.toString(reserved) +
                ", authPluginDataPart2=" + Arrays.toString(authPluginDataPart2) +
                ", authPluginName='" + authPluginName + '\'' +
                ", capabilities=" + capabilities +
                "}\n";
    }
}
