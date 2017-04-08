import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/3/27.
 */
public class MockMysqlConnection {
    @Test
    public void connection() throws IOException {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 3306);
        SocketChannel socketChannel = SocketChannel.open(address);
        //建立连接报文信息 来自wireshark(捕捉终端执行mysql -u root -p -h 127.0.0.1时对应的login request信息)
        String data = "4a0000000a352e37" +
                "2e31370010000000571a784122047f09" +
                "00fff7080200ff811500000000000000" +
                "00000034323a67033818626241582f00" +
                "6d7973716c5f6e61746976655f706173" +
                "73776f726400";
        byte[] byteArray = hexString2ByteArray(data);
        socketChannel.write(ByteBuffer.wrap(byteArray));
        ByteBuffer result = ByteBuffer.allocateDirect(1024);
        socketChannel.read(result);
        System.out.println(result);
    }

    @Test
    public void test() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3306);

        OutputStream out = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(out);
//建立连接报文信息 来自wireshark(捕捉终端执行mysql -u root -p -h 127.0.0.1时对应的login request信息)
        String hexs =
                "4a0000000a352e372e31370006000000" +
                        "1c62105c62613e6900fff7080200ff81" +
                        "15000000000000000000001a73590e3b" +
                        "027b5368144a0a006d7973716c5f6e61" +
                        "746976655f70617373776f726400";
        byte[] bytes = convertHexStrToByteArray(hexs); //将上述的16进制信息转为byte数组 如"bb"--> -69
        int packetLen = 191;
        bos.write(bytes, 0, packetLen);
        bos.flush();

//执行查询命令 select 'hello' 来自wireshark
        hexs = "0f0000000373656c656374202768656c6c6f27";
        bytes = convertHexStrToByteArray(hexs);
        bos.write(bytes, 0, 19);
        bos.flush();

//读取查询SQL的返回
        InputStream in = socket.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        byte[] buf = new byte[1024];
        int len = bis.read(buf);
        System.out.println(new String(buf, len - 14, 5));
    }

    byte[] convertHexStrToByteArray(String hexs) {
        byte[] a = new byte[hexs.length() / 2];
        int index = 0;
        for (int i = 0; i < hexs.length(); i += 2) {
            String e = hexs.substring(i, i + 2);
            a[index++] = convertIntToByte(Integer.valueOf(e, 16));
        }
        return a;
    }

    private byte convertIntToByte(int i) {
        if (i <= 127)
            return (byte) i;
        else
            return (byte) (i - 256);
    }

    public byte[] hexString2ByteArray(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; i++) {
            String temp = hex.substring(i * 2, (i + 1) * 2);
            result[i] = (byte) Integer.parseInt(temp, 16);
        }
        return result;
    }
}
