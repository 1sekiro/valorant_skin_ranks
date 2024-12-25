import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "SkinDetailsServlet", urlPatterns = "/api/skin")
public class SkinDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    @Override
    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/skindb");
        } catch (NamingException e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String skinName = request.getParameter("skinName"); // Get skinName from query parameter

        if (skinName == null || skinName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing skinName parameter.\"}");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT skin_name, win_num, loss_num, " +
                    "(CASE WHEN (win_num + loss_num) > 0 THEN ROUND(win_num * 100.0 / (win_num + loss_num), 2) ELSE 0 END) AS win_rate, icon " +
                    "FROM skin WHERE skin_name = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, skinName);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JsonObject skinDetails = new JsonObject();
                skinDetails.addProperty("skin_name", rs.getString("skin_name"));
                skinDetails.addProperty("win_num", rs.getInt("win_num"));
                skinDetails.addProperty("loss_num", rs.getInt("loss_num"));
                skinDetails.addProperty("win_rate", rs.getDouble("win_rate"));
                skinDetails.addProperty("icon", rs.getString("icon"));

                try (PrintWriter out = response.getWriter()) {
                    out.write(skinDetails.toString());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Skin not found.\"}");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}

