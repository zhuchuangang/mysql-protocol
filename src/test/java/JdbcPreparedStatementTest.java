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
            String url = "jdbc:mysql://localhost:8888";
            String username = "root";
            String password = "123456";
            Connection con = DriverManager.getConnection(url, username, password);
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM oauth_client_details WHERE client_id=?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, "client");
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                //Retrieve by column name
                String client_id = resultSet.getString("client_id");
                String resource_ids = resultSet.getString("resource_ids");
                String client_secret = resultSet.getString("client_secret");
                String scope = resultSet.getString("scope");
                //Display values
                System.out.print("client_id: " + client_id);
                System.out.print(", resource_ids: " + resource_ids);
                System.out.print(", client_secret: " + client_secret);
                System.out.println(", scope: " + scope);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }
}
