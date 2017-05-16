package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.constants.ClientCapabilityFlags;
import leader.us.mysql.protocol.packet.AuthPacket;
import leader.us.mysql.protocol.packet.HandshakePacket;
import leader.us.mysql.protocol.support.SecurityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zcg on 2017/5/2.
 */
public class BackendConnection extends NioConnection {

    private static Logger logger = LogManager.getLogger(BackendConnection.class);
    private DirectByteBufferPool pool;
    private SystemConfig config;
    private BackendHandler backendHandler;

    public BackendConnection(SocketChannel socketChannel, SystemConfig config, DirectByteBufferPool bufferPool) {
        super(socketChannel);
        pool = bufferPool;
        this.config = config;
    }

    public Chunk authentication() throws IOException {
        Chunk chunk = pool.getChunk(300);
        socketChannel.read(chunk.getBuffer());
        HandshakePacket handshake = new HandshakePacket();
        handshake.read(chunk.getBuffer());
        pool.recycleChunk(chunk);
        logger.info(handshake);

        AuthPacket ap = new AuthPacket();
        int len1 = handshake.authPluginDataPart1.length;
        int len2 = handshake.authPluginDataPart2.length;
        byte[] seed = new byte[len1 + len2];
        System.arraycopy(handshake.authPluginDataPart1, 0, seed, 0, len1);
        System.arraycopy(handshake.authPluginDataPart2, 0, seed, len1, len2);
        try {
            ap.password = SecurityUtil.scramble411(config.getPassword().getBytes(), seed);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        ap.packetSequenceId = 1;
        ap.capabilityFlags = ClientCapabilityFlags.getClientCapabilities();
        ap.characterSet = 0x53;//utf8_bin
        ap.username = config.getUsername();
        ap.database = config.getDatabase();
        ap.authPluginName = handshake.authPluginName;
        logger.info(ap);
        chunk = pool.getChunk(ap.calcPacketSize() + 4);
        ap.write(chunk.getBuffer());
        chunk.getBuffer().flip();
        return chunk;
    }

    public void setBackendHandler(BackendHandler backendHandler) {
        this.backendHandler = backendHandler;
    }

    public BackendHandler getBackendHandler() {
        return backendHandler;
    }
}
