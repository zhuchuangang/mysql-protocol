package leader.us.mysql.protocol.packet.passthou;

import leader.us.mysql.bufferpool.Chunk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by zcg on 2017/4/27.
 */
public class OkState extends PacketState {

    private ServerResponseContext context;

    public OkState(ServerResponseContext context) {
        this.context = context;
    }

    @Override
    public void read(SocketChannel channel) throws IOException {
        context.getHeader().position(0);
        int packetLength = context.getMessage().readUB3();
        Chunk chunk = context.getBufferPool().getChunk(packetLength + 4);
        ByteBuffer buffer = chunk.getBuffer();
        bufferLimit(buffer, packetLength);
        context.getHeader().position(0);
        buffer.put(context.getHeader());
        int readNum = channel.read(buffer);
        if (checkReadNum(readNum, channel)) {
            buffer.flip();
            context.getChunks().add(chunk);
            context.setState(null);
            return;
        }

        context.getHeader().clear();
        readNum = channel.read(context.getHeader());
        if (checkReadNum(readNum, channel)) {
            context.setState(null);
        }
    }
}
