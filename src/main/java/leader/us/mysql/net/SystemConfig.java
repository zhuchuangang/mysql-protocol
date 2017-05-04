package leader.us.mysql.net;

/**
 * Created by zcg on 2017/5/4.
 */
public class SystemConfig {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public SystemConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
