package leader.us.mysql.protocol.support;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

/**
 * java是大端的，mysql是小端的
 */
public class MySQLMessageForByte {

    public static final long NULL_LENGTH = -1;
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final byte[] data;
    private final int length;
    private int position;

    public MySQLMessageForByte(byte[] data) {
        this.data = data;
        this.length = data.length;
        this.position = 0;
    }

    /**
     * 获取byte数组的长度
     *
     * @return
     */
    public int length() {
        return length;
    }

    /**
     * 获取当前数组读取的位置
     *
     * @return
     */
    public int position() {
        return position;
    }

    /**
     * 移动位置
     *
     * @param i
     */
    public void move(int i) {
        position = position + i;
    }

    /**
     * 设置读取位置
     *
     * @param i
     */
    public void position(int i) {
        position = i;
    }

    /**
     * 数据是否读取完毕
     *
     * @return
     */
    public boolean hasRemaining() {
        return position < length;
    }

    /**
     * 读取当前位置的一个字节
     *
     * @return
     */
    public byte read() {
        if (position < length) {
            return data[position++];
        }
        return EMPTY_BYTES[0];
    }

    /**
     * 读取指定位置的一个字节
     *
     * @param i
     * @return
     */
    public byte read(int i) {
        if (i < length) {
            return data[i];
        }
        return EMPTY_BYTES[0];
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
    public long readUB4() {
        long i = read() & 0xff;
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
        if (position < length) {
            byte[] t = Arrays.copyOfRange(data, position, length);
            position = length - 1;
            return t;
        }
        return EMPTY_BYTES;
    }


    /**
     * 获取当前位置指定长度的字节数组
     *
     * @param length
     * @return
     */
    public byte[] readBytes(int length) {
        byte[] ba = new byte[length];
        System.arraycopy(data, position, ba, 0, length);
        return ba;
    }


    /**
     * 读取带有null的字节数组
     *
     * @return
     */
    public byte[] readBytesWithNull() {
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (data[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
            case -1:
                byte[] ba1 = new byte[length - position];
                System.arraycopy(data, position, ba1, 0, ba1.length);
                position = length;
                return ba1;
            case 0:
                position++;
                return EMPTY_BYTES;
            default:
                byte[] ba2 = new byte[offset - position];
                System.arraycopy(data, position, ba2, 0, ba2.length);
                position = offset + 1;
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
        System.arraycopy(data, position, ba, 0, length);
        position += length;
        return ba;
    }


    /**
     * 将剩余字节转为字符串
     *
     * @return
     */
    public String readString() {
        if (position > length) {
            return null;
        }
        String s = new String(data, position, length - position);
        position = length;
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
        if (position > length) {
            return null;
        }
        String s = new String(data, position, length - position, charset);
        position = length;
        return s;

    }

    /**
     * 读取带有null的字符串
     *
     * @return
     */
    public String readStringWithNull() {
        int offset = -1;
        if (position >= length) {
            return null;
        }
        for (int i = position; i < length; i++) {
            if (data[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
            case -1:
                String s1 = new String(data, position, length - position);
                position = length;
                return s1;
            case 0:
                position++;
                return null;
            default:
                String s2 = new String(data, position, offset - position);
                position = offset + 1;
                return s2;
        }
    }

    /**
     * 读取带有null的字符串
     *
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public String readStringWithNull(String charset) throws UnsupportedEncodingException {
        int offset = -1;
        if (position >= length) {
            return null;
        }
        for (int i = position; i < length; i++) {
            if (data[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
            case -1:
                String s1 = new String(data, position, length - position, charset);
                position = length;
                return s1;
            case 0:
                position++;
                return null;
            default:
                String s2 = new String(data, position, offset - position, charset);
                position = offset + 1;
                return s2;
        }
    }

    /**
     * 获取指定长度的字符
     *
     * @return
     */
    public String readStringWithLength() {
        int length = (int) readLength();
        if (length <= 0) {
            return null;
        }
        String s = new String(data, position, length);
        position += length;
        return s;
    }

    /**
     * 获取指定长度的字符
     *
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public String readStringWithLength(String charset) throws UnsupportedEncodingException {
        int length = (int) readLength();
        if (length <= 0) {
            return null;
        }
        String s = new String(data, position, length, charset);
        position += length;
        return s;
    }

    public java.sql.Time readTime() {
        move(6);
        int hour = read();
        int minute = read();
        int second = read();
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, hour, minute, second);
        return new Time(calendar.getTimeInMillis());
    }


    public java.util.Date readDate() {
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


    public BigDecimal readBigDecimal() {
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
