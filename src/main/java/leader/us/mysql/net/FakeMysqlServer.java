package leader.us.mysql.net;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.constants.StatusFlags;
import leader.us.mysql.protocol.packet.HandshakePacket;
import leader.us.mysql.protocol.support.RandomUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zcg on 2017/5/3.
 */
public class FakeMysqlServer {

    private static FakeMysqlServer mysqlServer;

    private HandshakePacket handshake;
    private AtomicInteger count = new AtomicInteger(0);

    private FakeMysqlServer() {
        // 生成认证数据
        byte[] authDataPart1 = RandomUtil.randomBytes(8);
        byte[] authDataPart2 = RandomUtil.randomBytes(12);
        handshake = new HandshakePacket();
        handshake.packetSequenceId = 0;
        handshake.protocolVersion = 0x0a;
        handshake.serverVersion = "5.5.5-10.1.14-MariaDB-1~jessie";
        //handshake.connectionId = count.incrementAndGet();
        //handshake.authPluginDataPart1 = authDataPart1;
        handshake.capabilities = getFakeServerCapabilities();
        handshake.capabilityLower = handshake.capabilities | 0xff;
        handshake.characterSet = 0x53;//utf8_bin
        handshake.statusFlags = StatusFlags.SERVER_STATUS_AUTOCOMMIT.getCode();
        handshake.capabilityUpper = handshake.capabilities >> 16;
        handshake.authPluginDataLen = 21;
        handshake.reserved = HandshakePacket.RESERVED_FILL;
        //handshake.authPluginDataPart2 = authDataPart2;
        handshake.authPluginName = "mysql_native_password";
    }

    public static FakeMysqlServer getInstance() {
        if (mysqlServer == null) {
            mysqlServer = new FakeMysqlServer();
        }
        return mysqlServer;
    }

    public HandshakePacket response() {
        // 生成认证数据
        byte[] authDataPart1 = RandomUtil.randomBytes(8);
        byte[] authDataPart2 = RandomUtil.randomBytes(12);
        handshake.connectionId = count.incrementAndGet();
        handshake.authPluginDataPart1 = authDataPart1;
        handshake.authPluginDataPart2 = authDataPart2;
        handshake.packetLength = handshake.calcPacketSize();
        return handshake;
    }

    public static final int getFakeServerCapabilities() {
        int flag = 0;
        //lower
        flag |= CapabilityFlags.CLIENT_LONG_PASSWORD.getCode();
        flag |= CapabilityFlags.CLIENT_FOUND_ROWS.getCode();
        flag |= CapabilityFlags.CLIENT_LONG_FLAG.getCode();
        flag |= CapabilityFlags.CLIENT_CONNECT_WITH_DB.getCode();
        flag |= CapabilityFlags.CLIENT_ODBC.getCode();
        flag |= CapabilityFlags.CLIENT_IGNORE_SPACE.getCode();
        flag |= CapabilityFlags.CLIENT_PROTOCOL_41.getCode();
        flag |= CapabilityFlags.CLIENT_INTERACTIVE.getCode();
        flag |= CapabilityFlags.CLIENT_IGNORE_SIGPIPE.getCode();
        flag |= CapabilityFlags.CLIENT_TRANSACTIONS.getCode();
        flag |= CapabilityFlags.CLIENT_SECURE_CONNECTION.getCode();
        //upper
        flag |= CapabilityFlags.CLIENT_MULTI_STATEMENTS.getCode();
        flag |= CapabilityFlags.CLIENT_MULTI_RESULTS.getCode();
        flag |= CapabilityFlags.CLIENT_PS_MULTI_RESULTS.getCode();
        flag |= CapabilityFlags.CLIENT_PLUGIN_AUTH.getCode();
        flag |= CapabilityFlags.CLIENT_CONNECT_ATTRS.getCode();
        flag |= CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getCode();
        return flag;
    }

}
