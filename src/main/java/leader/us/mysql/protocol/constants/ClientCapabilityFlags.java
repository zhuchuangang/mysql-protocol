package leader.us.mysql.protocol.constants;

/**
 * Created by zcg on 2017/4/28.
 */
public class ClientCapabilityFlags {
    public static final int getClientCapabilities() {
        int flag = 0;
        flag |= CapabilityFlags.CLIENT_LONG_PASSWORD.getCode();
        flag |= CapabilityFlags.CLIENT_FOUND_ROWS.getCode();
        flag |= CapabilityFlags.CLIENT_LONG_FLAG.getCode();
        flag |= CapabilityFlags.CLIENT_CONNECT_WITH_DB.getCode();
        flag |= CapabilityFlags.CLIENT_ODBC.getCode();
        flag |= CapabilityFlags.CLIENT_IGNORE_SPACE.getCode();
        flag |= CapabilityFlags.CLIENT_PROTOCOL_41.getCode();
        flag |= CapabilityFlags.CLIENT_INTERACTIVE.getCode();
        flag |= CapabilityFlags.CLIENT_IGNORE_SIGPIPE.getCode();
        flag |= CapabilityFlags.CLIENT_TRANSACTIONS.getCode();
        flag |= CapabilityFlags.CLIENT_SECURE_CONNECTION.getCode();
        flag |= CapabilityFlags.CLIENT_MULTI_RESULTS.getCode();
        flag |= CapabilityFlags.CLIENT_PS_MULTI_RESULTS.getCode();
        return flag;
    }
}
