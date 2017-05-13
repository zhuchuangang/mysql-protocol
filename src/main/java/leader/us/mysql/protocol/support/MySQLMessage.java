package leader.us.mysql.protocol.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * java是大端的，mysql是小端的
 */
public class MySQLMessage {

    public static final long NULL_LENGTH = -1;
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final ByteBuffer buffer;
    private final int length;

    public MySQLMessage(ByteBuffer byteBuffer) {
        byteBuffer.position(0);
        buffer = byteBuffer;
        length = buffer.capacity();
    }

    public byte[] data() {
        return buffer.array();
    }

    /**
     * 获取byte数组的长度
     *
     * @return
     */
    public int length() {
        return buffer.capacity();
    }

    /**
     * 获取当前数组读取的位置
     *
     * @return
     */
    public int position() {
        return buffer.position();
    }

    /**
     * 移动位置
     *
     * @param i
     */
    public void move(int i) {
        buffer.position(position() + i);
    }

    /**
     * 设置读取位置
     *
     * @param i
     */
    public void position(int i) {
        buffer.position(i);
    }

    /**
     * 数据是否读取完毕
     *
     * @return
     */
    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    /**
     * 读取当前位置的一个字节
     *
     * @return
     */
    public byte read() {
        return buffer.get();
    }

    /**
     * 读取指定位置的一个字节
     *
     * @param i
     * @return
     */
    public byte read(int i) {
        return buffer.get(i);
    }

    /**
     * 读取当前位置开始2个字节的无符号数
     *
     * @return
     */
    public int readUB2() {
        int i = read() & 0xff;
        i |= (read() & 0xff) << 8;
        return i;
    }

    /**
     * 读取当前位置开始3个字节的无符号数
     *
     * @return
     */
    public int readUB3() {
        int i = read() & 0xff;
        i |= (read() & 0xff) << 8;
        i |= (read() & 0xff) << 16;
        return i;
    }

    /**
     * 读取当前位置开始4个字节的无符号数
     *
     * @return
     */
    public int readUB4() {
        int i = read() & 0xff;
        i |= (read() & 0xff) << 8;
        i |= (read() & 0xff) << 16;
        i |= (read() & 0xff) << 24;
        return i;
    }


    /**
     * 读取当前位置开始的一个int类型的数
     * java是大端的，mysql是小端的,int类型占4个字节
     * http://blog.163.com/yurong_1987@126/blog/static/47517863200911314245752/
     *
     * @return
     */
    public int readInt() {
        int i = read() & 0xff;
        i |= (read() & 0xff) << 8;
        i |= (read() & 0xff) << 16;
        i |= (read() & 0xff) << 24;
        return i;
    }


    /**
     * 读取当前位置开始的4个字节的float类型
     *
     * @return
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * 读取当前位置开始的8个字节的long类型
     *
     * @return
     */
    public long readLong() {
        long i = read() & 0xff;
        i |= (read() & 0xff) << 8;
        i |= (read() & 0xff) << 16;
        i |= (read() & 0xff) << 24;
        i |= (read() & 0xff) << 32;
        i |= (read() & 0xff) << 40;
        i |= (read() & 0xff) << 48;
        i |= (read() & 0xff) << 56;
        return i;
    }

    /**
     * 读取当前位置开始的8个字节的double类型
     *
     * @return
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }


    /**
     * 读取报文长度
     *
     * @return
     */
    public long readLength() {
        int length = read() & 0xff;
        switch (length) {
            case 251:
                return NULL_LENGTH;
            case 252:
                return readUB2();
            case 253:
                return readUB3();
            case 254:
                return readLong();
            default:
                return length;
        }
    }

    /**
     * 读取剩余的字节
     *
     * @return
     */
    public byte[] readBytes() {
        int length = buffer.remaining();
        if (length <= 0) {
            return EMPTY_BYTES;
        }
        return readBytes(length);
    }


    /**
     * 获取当前位置指定长度的字节数组
     *
     * @param length
     * @return
     */
    public byte[] readBytes(int length) {
        byte[] ba = new byte[length];
        buffer.get(ba);
        return ba;
    }


    /**
     * 读取带有null的字节数组
     *
     * @return
     */
    public byte[] readBytesWithNull() {
        int length = buffer.remaining();
        if (length <= 0) {
            return EMPTY_BYTES;
        }
        int offset = -1;
        int position = position();
        int limit = buffer.limit();
        for (int i = position; i < limit; i++) {
            if (buffer.get(i) == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
            case -1:
                byte[] ba1 = new byte[length];
                buffer.get(ba1);
                return ba1;
            case 0:
                buffer.position(position + 1);
                return EMPTY_BYTES;
            default:
                byte[] ba2 = new byte[offset - position];
                buffer.get(ba2);
                buffer.get();
                return ba2;

        }
    }

    /**
     * 读取长度编码整型
     *
     * @return
     */
    public byte[] readBytesWithLength() {
        int length = (int) readLength();
        if (length == NULL_LENGTH) {
            return null;
        }
        if (length <= 0) {
            return EMPTY_BYTES;
        }
        byte[] ba = new byte[length];
        buffer.get(ba);
        return ba;
    }


    /**
     * 将剩余字节转为字符串
     *
     * @return
     */
    public String readString() {
        int remain = buffer.remaining();
        if (remain == 0) {
            return null;
        }
        String s = new String(readBytes());
        return s;
    }

    /**
     * 将剩余字节转为指定类型的字符串
     *
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public String readString(String charset) throws UnsupportedEncodingException {
        int remain = buffer.remaining();
        if (remain == 0) {
            return null;
        }
        String s = new String(readBytes(), charset);
        return s;

    }

    /**
     * 读取带有null的字符串
     *
     * @return
     */
    public String readStringWithNull() {
        byte[] bytes = readBytesWithNull();
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }

    /**
     * 读取带有null的字符串
     *
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public String readStringWithNull(String charset) throws UnsupportedEncodingException {
        byte[] readed = readBytesWithNull();
        return (EMPTY_BYTES == readed) ? null : new String(readed, charset);
    }

    /**
     * 获取指定长度的字符
     *
     * @return
     */
    public String readStringWithLength() {
        byte[] ba = readBytesWithLength();
        if (ba != null) {
            String s = new String(ba);
            return s;
        }
        return null;
    }

    /**
     * 获取指定长度的字符
     *
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public String readStringWithLength(String charset) throws UnsupportedEncodingException {
        byte[] ba = readBytesWithLength();
        if (ba != null) {
            String s = new String(ba, charset);
            return s;
        }
        return null;
    }

    public Time readTime() {
        move(6);
        int hour = read();
        int minute = read();
        int second = read();
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, hour, minute, second);
        return new Time(calendar.getTimeInMillis());
    }


    public Date readDate() {
        byte length = read();
        int year = readUB2();
        byte month = read();
        byte date = read();
        int hour = read();
        int minute = read();
        int second = read();
        if (length == 11) {
            long nanos = readUB4();
            Calendar cal = getLocalCalendar();
            cal.set(year, --month, date, hour, minute, second);
            Timestamp time = new Timestamp(cal.getTimeInMillis());
            time.setNanos((int) nanos);
            return time;
        } else {
            Calendar cal = getLocalCalendar();
            cal.set(year, --month, date, hour, minute, second);
            return new java.sql.Date(cal.getTimeInMillis());
        }
    }


    public BigDecimal readBigDecimal() throws IOException {
        String src = readStringWithLength();
        return src == null ? null : new BigDecimal(src);
    }

    private static final ThreadLocal<Calendar> localCalendar = new ThreadLocal<Calendar>();

    private static final Calendar getLocalCalendar() {
        Calendar cal = localCalendar.get();
        if (cal == null) {
            cal = Calendar.getInstance();
            localCalendar.set(cal);
        }
        return cal;
    }
}
