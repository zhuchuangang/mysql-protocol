package leader.us.mysql.net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zcg on 2017/5/4.
 */
public class BackendConnectionPool {
    private static BackendConnectionPool pool;
    private List<BackendConnection> connections;

    private BackendConnectionPool() {
        this.connections = new ArrayList<>();
    }

    public static BackendConnectionPool getInstance() {
        if (pool == null) {
            pool = new BackendConnectionPool();
        }
        return pool;
    }

    public void addConnection(BackendConnection connection) {
        this.connections.add(connection);
    }

    public BackendConnection connection() {
        if (!this.connections.isEmpty()) {
            int index = ThreadLocalRandom.current().nextInt(this.connections.size());
            return this.connections.get(index);
        } else {
            return null;
        }
    }
}
