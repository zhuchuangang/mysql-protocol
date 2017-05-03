package leader.us.mysql;

import leader.us.mysql.protocol.constants.ClientCapabilityFlags;
import leader.us.mysql.protocol.constants.CommandTypes;
import leader.us.mysql.protocol.constants.StatusFlags;
import leader.us.mysql.protocol.packet.AuthPacket;
import leader.us.mysql.protocol.packet.EOFPacket;
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
import java.util.ArrayList;
import java.util.List;

/**
 * test 1:
 * DROP PROCEDURE IF EXISTS multi;
 * DELIMITER $$
 * CREATE PROCEDURE multi() BEGIN
 * select * from users;
 * END$$
 * DELIMITER ;
 * <p>
 * CALL multi();
 * <p>
 * test 2:
 * CREATE TEMPORARY TABLE ins(id INT);
 * DROP PROCEDURE IF EXISTS multi;
 * DELIMITER $$
 * CREATE PROCEDURE multi() BEGIN
 * SELECT 1;
 * select * from users;
 * INSERT INTO ins VALUES (1);
 * INSERT INTO ins VALUES (1),(2),(3);
 * END$$
 * DELIMITER ;
 * <p>
 * CALL multi();
 * DROP TABLE ins;
 * <p>
 * test 3:
 * CREATE TABLE ins(id INT);
 * DROP PROCEDURE IF EXISTS multi_out;
 * DELIMITER $$
 * CREATE PROCEDURE multi_out(a INT,b INT,OUT result INT) BEGIN
 * SELECT 1;
 * set result=a+b;
 * INSERT INTO ins VALUES (1);
 * INSERT INTO ins VALUES (1),(2),(3);
 * END$$
 * DELIMITER ;
 * <p>
 * CALL multi_out(1,2,@c);
 * select @c;
 * DROP TABLE ins;
 */
public class StoredProcedurePacketTest {
    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        int port = 3306;
        String username = "root";
        String password = "123456";
        String database = "uaa";
        List<String> sqls = new ArrayList<>();
        sqls.add("CALL multi();");
//        sqls.add("CALL multi_out(1,2,@c);");
//        sqls.add("select @c;");

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
        ap.capabilityFlags = ClientCapabilityFlags.getClientCapabilities();
        ap.maxPacket = 0x1 << 24 - 1;
        ap.characterSet = 0x53;//utf8_bin
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

        data = new byte[1024];
        in.read(data);


        if (data[4] == 0x00) {
            for (String sql : sqls) {
                System.out.println("exec " + sql);
                ByteBuffer cp = ByteBuffer.allocate(sql.length() + 5);
                BufferUtil.writeUB3(cp, sql.length());
                cp.put((byte) 0);
                cp.put(CommandTypes.COM_QUERY);
                cp.put(sql.getBytes());
                out.write(cp.array());

                for (; ; ) {
                    System.out.println("for ;;");
                    byte[] resultByte = new byte[1024];
                    int readSize = in.read(resultByte);
                    ByteBuffer mrs = ByteBuffer.wrap(resultByte, 0, readSize);

                    MultiResultSetContext context = new MultiResultSetContext();
                    List<MySQLPacket> packets = context.read(mrs);
                    boolean moreResults = false;
                    for (int i = 0; i < packets.size(); i++) {
                        MySQLPacket p = packets.get(i);
                        System.out.println(p);
                        if (i == packets.size() - 1) {
                            if (p instanceof EOFPacket) {
                                int flag = (((EOFPacket) p).statusFlags) & StatusFlags.SERVER_MORE_RESULTS_EXISTS.getCode();
                                if (flag == StatusFlags.SERVER_MORE_RESULTS_EXISTS.getCode()) {
                                    moreResults = true;
                                    System.out.println("more results");
                                }
                            } else {
                                moreResults = false;
                                System.out.println("no more results");
                            }
                        }
                    }
                    if (!moreResults) {
                        System.out.println("break...");
                        break;
                    }
                }
                System.out.println("================" + sql + "================");
            }
        }
        if (data[4] == (byte) 0xff) {
            System.out.println("error");
        }
        if (data[4] == 0xfe) {
            System.out.println("EOF");
        }
    }
}
