package leader.us.mysql.net;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zcg on 2017/5/11.
 */
public class Session {
    private Map<Integer,Integer> stmtIdParamCount=new HashMap<>();
    private boolean stmtPrepare=false;

    public Map<Integer, Integer> getStmtIdParamCount() {
        return stmtIdParamCount;
    }

    public void setStmtIdParamCount(Map<Integer, Integer> stmtIdParamCount) {
        this.stmtIdParamCount = stmtIdParamCount;
    }

    public boolean getStmtPrepare() {
        return stmtPrepare;
    }

    public void setStmtPrepare(boolean stmtPrepare) {
        this.stmtPrepare = stmtPrepare;
    }
}
