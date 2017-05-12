package leader.us.mysql.net;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zcg on 2017/5/11.
 */
public class Session {
    private Map<Integer,Integer> stmtIdParamCount=new HashMap<>();

    public Map<Integer, Integer> getStmtIdParamCount() {
        return stmtIdParamCount;
    }

    public void setStmtIdParamCount(Map<Integer, Integer> stmtIdParamCount) {
        this.stmtIdParamCount = stmtIdParamCount;
    }
}
