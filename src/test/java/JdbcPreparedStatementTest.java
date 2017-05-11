import org.junit.Test;

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
}
