package leader.us.mysql.bufferpool;

import java.nio.ByteBuffer;

/**
 * Created by zcg on 2017/3/26.
 */
public class Chunk {
    private int chunkSize;
    private int chunkIndex;
    private ByteBuffer buffer;

    public Chunk(int chunkSize, int chunkIndex, ByteBuffer buffer) {
        this.chunkSize = chunkSize;
        this.chunkIndex = chunkIndex;
        this.buffer = buffer;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "chunkSize=" + chunkSize +
                ", chunkIndex=" + chunkIndex +
                ", buffer=" + buffer +
                '}';
    }
}
