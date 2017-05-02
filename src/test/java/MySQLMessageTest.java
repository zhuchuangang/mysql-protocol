import leader.us.mysql.protocol.support.MySQLMessageForByte;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zcg on 2017/3/30.
 */
public class MySQLMessageTest {

    @Test
    public void testReadInt() {
        //1+256*128+65536+16777216=16875521
        //http://blog.163.com/yurong_1987@126/blog/static/47517863200911314245752/
        byte[] ba = new byte[]{1, -128, 1, 1};
        MySQLMessageForByte message = new MySQLMessageForByte(ba);
        int result = message.readInt();
        Assert.assertEquals(16875521, result);
    }

    @Test
    public void testReadUB4() {
        //1+256*128+65536+16777216=16875521
        //http://blog.163.com/yurong_1987@126/blog/static/47517863200911314245752/
        byte[] ba = new byte[]{1, -128, 1, 1};
        MySQLMessageForByte message = new MySQLMessageForByte(ba);
        long result = message.readUB4();
        Assert.assertEquals(16875521, result);
    }

    @Test
    public void testReadBytes() {
        byte[] ba = new byte[]{1, 1, 1, 1};
        MySQLMessageForByte message = new MySQLMessageForByte(ba);
        byte[] temp = message.readBytes();
        assert ba.length == temp.length;
    }


}
