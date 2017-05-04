package leader.us.mysql.net;

import leader.us.mysql.bufferpool.Chunk;
import leader.us.mysql.bufferpool.DirectByteBufferPool;
import leader.us.mysql.protocol.constants.ErrorCode;
import leader.us.mysql.protocol.constants.StatusFlags;
import leader.us.mysql.protocol.packet.AuthPacket;
import leader.us.mysql.protocol.packet.ERRPacket;
import leader.us.mysql.protocol.packet.HandshakePacket;
import leader.us.mysql.protocol.packet.OKPacket;
import leader.us.mysql.protocol.packet.mutli.ErrorState;
import leader.us.mysql.protocol.support.SecurityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zcg on 2017/5/4.
 */
public class FakeLoginAuthenticationHandler implements SqlCommandHandler {
    private static Logger logger = LogManager.getLogger(FakeLoginAuthenticationHandler.class);
    private DirectByteBufferPool bufferPool;

    public FakeLoginAuthenticationHandler(DirectByteBufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    public Chunk response(Chunk chunk, SocketChannel socketChannel, FrontendHandler handler) {
        AuthPacket ap = new AuthPacket();
        ap.read(chunk.getBuffer());
        bufferPool.recycleChunk(chunk);
        if (logger.isDebugEnabled()) {
            logger.debug("client auth packet:{}", ap);
        }
        boolean success = false;
        FakeMysqlServer server = FakeMysqlServer.getInstance();
        if (ap.username.equals(server.getUsername())) {
            HandshakePacket handshake = server.getHandshake();
            int len1 = handshake.authPluginDataPart1.length;
            int len2 = handshake.authPluginDataPart2.length;
            byte[] seed = new byte[len1 + len2];
            System.arraycopy(handshake.authPluginDataPart1, 0, seed, 0, len1);
            System.arraycopy(handshake.authPluginDataPart2, 0, seed, len1, len2);
            String serverEncryptedPassword = null;
            try {
                serverEncryptedPassword = new String(SecurityUtil.scramble411(server.getPassword().getBytes(), seed));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            String clientEncryptedPassword = new String(ap.password);
            if (clientEncryptedPassword.equals(serverEncryptedPassword)) {
                success = true;
            }
        }
        if (success) {
            OKPacket op = new OKPacket();
            op.packetSequenceId = 2;
            op.capabilities = FakeMysqlServer.getFakeServerCapabilities();
            op.statusFlags = StatusFlags.SERVER_STATUS_AUTOCOMMIT.getCode();
            chunk = bufferPool.getChunk(op.calcPacketSize() + 4);
            op.write(chunk.getBuffer());
            chunk.getBuffer().flip();
            if (logger.isDebugEnabled()) {
                logger.debug("login authentication success,server response client ok packet:{}", op);
            }
            handler.setCommandHandler(new NormalSchemaSqlCommandHandler());
        } else {
            ERRPacket ep = new ERRPacket();
            ep.packetSequenceId = 2;
            ep.capabilities = FakeMysqlServer.getFakeServerCapabilities();
            ep.errorCode = ErrorCode.ER_ACCESS_DENIED_ERROR;
            ep.sqlState = "28000";
            String address = "";
            try {
                address = socketChannel.getLocalAddress().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ep.errorMessage = "Access denied for user '" + ap.username + "'@'" + address + "' (using password: YES)";
            chunk = bufferPool.getChunk(ep.calcPacketSize() + 4);
            ep.write(chunk.getBuffer());
            chunk.getBuffer().flip();
            if (logger.isDebugEnabled()) {
                logger.debug("login authentication error,server response client error packet:{}", ep);
            }
        }
        return chunk;
    }
}
