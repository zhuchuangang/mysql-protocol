package leader.us.mysql.protocol.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zcg on 2017/4/3.
 */
public class SecurityUtil {

    /**
     * https://dev.mysql.com/doc/internals/en/secure-password-authentication.html
     * http://www.jianshu.com/p/651fb39c0a51
     *
     * @param pass
     * @param seed
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static final byte[] scramble411(byte[] pass, byte[] seed)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] pass1 = md.digest(pass);
        md.reset();
        byte[] pass2 = md.digest(pass1);
        md.reset();
        md.update(seed);
        byte[] pass3 = md.digest(pass2);
        for (int i = 0; i < pass3.length; i++) {
            pass3[i] = (byte) (pass3[i] ^ pass1[i]);
        }
        return pass3;
    }
}
