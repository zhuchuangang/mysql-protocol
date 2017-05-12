import org.junit.Test;
import org.mariadb.jdbc.internal.packet.result.OkPacket;

import java.sql.*;

/**
 * Created by zcg on 2017/5/5.
 */
public class JdbcPreparedStatementTest {

    @Test
    public void test() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/uaa";
            String username = "root";
            String password = "123456";
            Connection con = DriverManager.getConnection(url, username, password);
            String sql = "SELECT * FROM users WHERE username=?;";
            //Statement s=con.createStatement();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, "admin");
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                //Retrieve by column name
                int id = resultSet.getInt("id");
                String u = resultSet.getString("username");
                String p = resultSet.getString("password");
                //Display values
                System.out.print("id: " + id);
                System.out.print(", username: " + u);
                System.out.println(", password: " + p);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @Test
    public void test1() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/uaa";
            String username = "root";
            String password = "123456";
            Connection con = DriverManager.getConnection(url, username, password);
            String sql = "SELECT * FROM roles WHERE id=?;";
            //Statement s=con.createStatement();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, 1);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                //Retrieve by column name
                int id = resultSet.getInt("id");
                String appkey = resultSet.getString("appkey");
                String name = resultSet.getString("name");
                //Display values
                System.out.print("id: " + id);
                System.out.print(", appkey: " + appkey);
                System.out.println(", name: " + name);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }
}
