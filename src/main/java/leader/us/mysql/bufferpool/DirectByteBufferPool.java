package leader.us.mysql.bufferpool;

import java.util.TreeSet;

/**
 * Created by zcg on 2017/3/25.
 */
public class DirectByteBufferPool {

    public static final double GROWTH_FACTOR = 1.25;
    public static final int MIN_SIZE = 4;
    private static DirectByteBufferPool pool;
    private Slab temp;
    private TreeSet<Slab> slabClass;


    private DirectByteBufferPool() {
        this.slabClass = new TreeSet<Slab>();
        this.temp = new Slab();
        int size = MIN_SIZE;
        while (size < Slab.PAGE_SIZE) {
            Slab slab = new Slab(size);
            slabClass.add(slab);
            size = (int) (size * GROWTH_FACTOR);
        }
        if (size >= Slab.PAGE_SIZE) {
            Slab slab = new Slab(Slab.PAGE_SIZE);
            slabClass.add(slab);
        }
    }

    public static DirectByteBufferPool getInstance() {
        if (pool == null) {
            pool = new DirectByteBufferPool();
        }
        return pool;
    }

    public synchronized Chunk getChunk(int size) {
        temp.setChunkSize(size);
        Slab slab = slabClass.ceiling(temp);
        return slab.getChunk();
    }

    public synchronized void recycleChunk(Chunk chunk) {
        temp.setChunkSize(chunk.getChunkSize());
        Slab slab = slabClass.ceiling(temp);
        slab.recycleChunk(chunk);
    }

    @Override
    public String toString() {
        return "DirectByteBufferPool{" +
                "slabClass=" + slabClass +
                '}';
    }
}
