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
            String sql = "SELECT * FROM test where id=?;";
            //Statement s=con.createStatement();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, 0);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                //Retrieve by column name
                int id = resultSet.getInt("id");
                String v1 = resultSet.getString("c1");
                String v2 = resultSet.getString("c2");
                //Display values
                System.out.print("id: " + id);
                System.out.print(", v1: " + v1);
                System.out.println(", v2: " + v2);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }
}
