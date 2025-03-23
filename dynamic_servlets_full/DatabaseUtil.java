
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.google.gson.Gson;

public class DatabaseUtil {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/servlet_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static ServletConfigJson getServletConfigurationFromJson() {
        String query = "SELECT config_json FROM servlet_config WHERE active = 1 LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                String jsonConfig = rs.getString("config_json");
                Gson gson = new Gson();
                return gson.fromJson(jsonConfig, ServletConfigJson.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
