import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/3/26.
 */
public class BitTest {
    @Test
    public void test() {
        System.out.println(2 >> 1);
        System.out.println(2 >>> 1);
        System.out.println(4 << 1);
        System.out.println(-1 >>> 1);

        byte a = (byte) 255;
        System.out.println(a & 0xFF);

        byte b = (byte) 0xf0;
        System.out.println(b);
        int c = 0xff & a;
        System.out.println(c);

        int flag = 0B01101000;
        byte d = (byte) ((flag & 0B00110000) >>> 4);
        System.out.println(d);
    }


    @Test
    public void intToByteTest() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(10213);
        byte[] t = bb.array();
        for (int i = 0; i < t.length; i++) {
            System.out.printf("%x\n", t[i]);
        }
    }

    @Test
    public void oddTest() {
        System.out.println((1 & 1) == 0);
        System.out.println((2 & 1) == 0);
    }


    @Test
    public void negativeTest() {
        System.out.println(1 >> 31);
        System.out.println(-1 >> 31);
    }

    @Test
    public void nonTest() {
        System.out.println(~1);
        System.out.println(~-1);
    }


    @Test
    public void noxTest() {
        System.out.println((1 ^ 1) > 0);
    }
}
