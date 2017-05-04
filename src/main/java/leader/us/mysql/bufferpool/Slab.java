package leader.us.mysql.bufferpool;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Created by zcg on 2017/3/25.
 */
public class Slab implements Comparable<Slab> {
    public static final int PAGE_SIZE = 1024 * 1024;
    private int chunkSize;
    private LinkedList<ByteBuffer> pages;
    private BitSet usageState;

    public Slab() {
    }

    public Slab(int chunkSize) {
        this.chunkSize = chunkSize;
        this.pages = new LinkedList<>();
        this.usageState = new BitSet();
    }

    public Chunk getChunk() {
        int index = -1;
        for (int i = 0; i < usageState.length(); i++) {
            if (!usageState.get(i)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            index = usageState.length();
        }
        this.usageState.set(index);
//        System.out.println("set usageState index " + index + " value is " + this.usageState.get(index));
//        System.out.println("chunkSize=" + chunkSize);
        int pageIndex = index * chunkSize / PAGE_SIZE;
//        System.out.println("pageIndex=" + pageIndex);
        if (pageIndex + 1 > pages.size()) {
            pages.add(ByteBuffer.allocateDirect(PAGE_SIZE));
//            System.out.println("add page");
        }
        ByteBuffer page = pages.get(pageIndex);
//        System.out.println("pageIndex=" + pageIndex + " page is " + page);
        int position = (index * chunkSize) % PAGE_SIZE;
//        System.out.println("position=" + position);
        int limit = ((index + 1) * chunkSize) % PAGE_SIZE;
        //System.out.println("limit=" + page.limit()+"  position="+position);
        page.limit(limit);
        page.position(position);
        ByteBuffer bb = page.slice();
//        System.out.println("page.slice() is " + bb);
        return new Chunk(chunkSize, index, bb);
    }

    public void recycleChunk(Chunk chunk) {
//        System.out.println("recycle chunk index is " + chunk.getChunkIndex());
//        System.out.println("begin recycle");
        this.usageState.clear(chunk.getChunkIndex());
//        System.out.println("get usageState index " + chunk.getChunkIndex() + " value is " + this.usageState.get(chunk.getChunkIndex()));
//        System.out.println("end recycle");
    }


    @Override
    public int compareTo(Slab o) {
        return this.chunkSize - o.chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        if (pages == null) {
            this.chunkSize = chunkSize;
        }
    }
}
