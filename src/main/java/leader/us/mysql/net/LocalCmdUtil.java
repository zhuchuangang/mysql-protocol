package leader.us.mysql.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zcg on 2017/3/20.
 */
public class LocalCmdUtil {


    public static String callCmdAndGetResult(String cmd) {
        StringBuffer result = new StringBuffer();
        try {
            ProcessBuilder sb = new ProcessBuilder(cmd.split("\\s"));
            Process process = sb.start();
            InputStream in = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String temp;
            while ((temp = br.readLine()) != null) {
                result.append(temp + "\r\n");
            }

            InputStream error = process.getErrorStream();
            InputStreamReader esr = new InputStreamReader(error);
            BufferedReader ebr = new BufferedReader(esr);
            while ((temp = ebr.readLine()) != null) {
                result.append(temp+ "\r\n");
            }
            br.close();
            isr.close();
            in.close();
            ebr.close();
            esr.close();
            error.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
