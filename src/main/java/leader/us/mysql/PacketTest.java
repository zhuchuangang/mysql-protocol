package leader.us.mysql;

import leader.us.mysql.protocol.constants.CapabilityFlags;
import leader.us.mysql.protocol.constants.CommandTypes;
import leader.us.mysql.protocol.constants.StatusFlags;
import leader.us.mysql.protocol.packet.*;
import leader.us.mysql.protocol.support.BufferUtil;
import leader.us.mysql.protocol.support.SecurityUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zcg on 2017/4/2.
 */
public class PacketTest {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 3306;
        String username = "root";
        String password = "123456";
        String database = "uaa";
//        String sql = "select * from users";
//        String sql = "select * from test_integer";
//        String sql = "select * from test_float";
//        String sql = "select * from test_date";
//        String sql = "select * from test_char";
        String sql = "select * from test_blob";

        InetSocketAddress address = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        StringBuilder sb = new StringBuilder();
        ByteBuffer hpBuffer = ByteBuffer.allocate(1024);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            //连接mysql
            socket.connect(address);
            //获得输入流
            InputStream in = socket.getInputStream();
            //获得输出流
            OutputStream out = socket.getOutputStream();
            byte[] data = new byte[1024];
            //读取服务端发给客户端的握手包数据
            in.read(data);
            hpBuffer.put(data);
            hpBuffer.flip();
            //服务端发给客户端的握手包
            HandshakePacket hp = new HandshakePacket();
            hp.read(hpBuffer);
            //打印握手包内容
            System.out.println("HandshakePacket:" + hp);
            sb.append("HandshakePacket:\n" + hp + "\n");

            //向服务端发送认证包
            AuthPacket ap = new AuthPacket();
            //设置认证包内容
            ap.packetSequenceId = 1;
            ap.capabilityFlags = getClientCapabilities();
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
            //发送认证包
            out.write(apBuffer.array(), 0, apBuffer.limit());
            out.flush();
            System.out.println("AuthPacket:" + ap);
            sb.append("\nAuthPacket:\n" + ap + "\n");


            //接受服务端响应信息，可能是OKPacket或是ERRPacket
            buffer.clear();
            int length = in.read(buffer.array());
            buffer.limit(length);

            int status = buffer.get(4) & 0xff;
            System.out.println("result status:" + status);

            if (status == 0xff) {
                ERRPacket ep = new ERRPacket();
                ep.capabilities = hp.capabilities;
                ep.read(buffer);
                System.out.println("ERRPacket:" + ep);
                sb.append("\nERRPacket:\n" + ep + "\n");
            }

            if (status == 0x00) {
                OKPacket op = new OKPacket();
                op.read(buffer);
                System.out.println("OKPacket:" + op);
                sb.append("\nOKPacket:\n" + op + "\n");
                if (op.statusFlags == StatusFlags.SERVER_STATUS_AUTOCOMMIT.getCode()) {
                    System.out.println("login success,status:server status autocommit");
                } else {
                    System.out.println("login success,status:" + op.statusFlags);
                }

                ByteBuffer queryBuffer = ByteBuffer.allocate(4 + 1 + sql.length());
                BufferUtil.writeUB3(queryBuffer, sql.length() + 1);
                queryBuffer.put((byte) 0);
                queryBuffer.put(CommandTypes.COM_QUERY);
                queryBuffer.put(sql.getBytes());
                queryBuffer.flip();
                out.write(queryBuffer.array());
                System.out.println("sql query:" + sql);
                sb.append("\nsql query:\n" + sql + "\n");


                //https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-ProtocolText::Resultset
                buffer.clear();
                length = in.read(buffer.array());
                buffer.limit(length);
                status = buffer.get(4) & 0xff;
                if (status == 0xff) {
                    System.out.println("result status2:" + status);
                    ERRPacket ep = new ERRPacket();
                    ep.capabilities = hp.capabilities;
                    ep.read(buffer);
                    System.out.println("ERRPacket:" + ep);
                    sb.append("\nERRPacket:\n" + ep + "\n");
                } else {
                    ResultSetPacket rsrp = new ResultSetPacket();
                    rsrp.read(buffer);
                    System.out.println("ResultSetPacket:" + rsrp);
                    sb.append("\nResultSetPacket:\n" + rsrp + "\n");
                }
            }
            writeToFile(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
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
        return flag;
    }

    public static final void writeToFile(String log) {
        try {
            RandomAccessFile file = new RandomAccessFile("mysql-packet.log", "rw");
            FileChannel channel = file.getChannel();
            channel.write(ByteBuffer.wrap(log.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
