package leader.us.mysql;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.packet.AuthPacket;
import leader.us.mysql.protocol.packet.HandshakePacket;
import leader.us.mysql.protocol.packet.MySQLPacket;
import leader.us.mysql.protocol.packet.mutli.MultiResultSetContext;
import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.SecurityUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zcg on 2017/4/26.
 */
public class StoredProcedurePacketTest {
    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        int port = 3306;
        String username = "root";
        String password = "123456";
        String database = "uaa";
        String sql = "CALL multi_out(1,2,@c);";

        InetSocketAddress address = new InetSocketAddress(ip, port);
        Socket socket = new Socket();
        socket.connect(address);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        byte[] data = new byte[1024];
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        in.read(data);
        HandshakePacket hp = new HandshakePacket();
        readBuffer.put(data);
        hp.read(readBuffer);
        System.out.println(hp);

        AuthPacket ap = new AuthPacket();
        ap.packetSequenceId = 1;
        ap.capabilityFlags = getClientCapabilities();
        ap.maxPacket = 0x1 << 24 - 1;
        ap.characterSet = hp.characterSet;
        ap.username = username;
        //加密登录密码
        int len1 = hp.authPluginDataPart1.length;
        int len2 = hp.authPluginDataPart2.length;
        byte[] seed = new byte[len1 + len2];
        System.arraycopy(hp.authPluginDataPart1, 0, seed, 0, len1);
        System.arraycopy(hp.authPluginDataPart2, 0, seed, len1, len2);
        try {
            ap.password = SecurityUtil.scramble411(password.getBytes(), seed);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        ap.database = database;
        ap.authPluginName = hp.authPluginName;
        ByteBuffer apBuffer = ByteBuffer.allocate(ap.calcPacketSize() + 4);
        ap.write(apBuffer);
        out.write(apBuffer.array());

        in.read(data);
        if (data[4] == 0x00) {
            System.out.println("success");
            ByteBuffer cp = ByteBuffer.allocate(sql.length() + 5);
            BufferUtil.writeUB3(cp, sql.length());
            cp.put((byte) 0);
            cp.put((byte) 3);
            cp.put(sql.getBytes());
            out.write(cp.array());

            byte[] resultByte = new byte[1024];
            in.read(resultByte);
            ByteBuffer mrs = ByteBuffer.allocate(1024);
            mrs.put(resultByte);
            mrs.flip();
            MultiResultSetContext context=new MultiResultSetContext();
            MySQLPacket p=context.read(mrs);
            System.out.println(p);
            mrs=mrs.slice();
            p=context.read(mrs);
            System.out.println(p);
            mrs=mrs.slice();
            p=context.read(mrs);
            System.out.println(p);
            mrs=mrs.slice();
            p=context.read(mrs);
            System.out.println(p);
        }
        if (data[4] == (byte) 0xff) {
            System.out.println("error");
        }
        if (data[4] == 0xfe) {
            System.out.println("EOF");
        }
    }

    public static final int getClientCapabilities() {
        int flag = 0;
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
        flag |= CapabilityFlags.CLIENT_MULTI_RESULTS.getCode();
        flag |= CapabilityFlags.CLIENT_PS_MULTI_RESULTS.getCode();

        return flag;
    }
}
