import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "RankServlet", urlPatterns = "/api/rank")
public class RankServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/skindb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String weaponName = request.getParameter("weaponName");

        try (Connection conn = dataSource.getConnection()) {
            String query;
            PreparedStatement statement;

            if (weaponName == null || weaponName.isEmpty()) {
                weaponName = "vandal";
            }

            query = "SELECT s.skin_name, s.win_num, s.icon, w.weapon_name, w.icon as weapon_icon " +
                    "FROM skin s JOIN weapon w ON s.weapon_id = w.weapon_id " +
                    "WHERE w.weapon_name = ? ORDER BY s.win_num DESC";
            statement = conn.prepareStatement(query);
            statement.setString(1, weaponName.toLowerCase());

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("skin_name", rs.getString("skin_name"));
                jsonObject.addProperty("win_num", rs.getInt("win_num"));
                jsonObject.addProperty("icon", rs.getString("icon"));
                jsonObject.addProperty("weapon_name", rs.getString("weapon_name"));
                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
    }
}