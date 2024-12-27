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
        // Add CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String skinName = request.getParameter("skinName");

        System.out.println("Received request for skin: " + skinName);

        if (skinName == null || skinName.isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing skinName parameter.");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT skin_name, win_num, vote_count, " +
                    "(CASE WHEN vote_count > 0 THEN ROUND(win_num * 100.0 / vote_count, 2) ELSE 0 END) AS win_rate, icon " +
                    "FROM skin WHERE skin_name = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, skinName);
                System.out.println("Executing query: " + query + " with parameter: " + skinName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        JsonObject skinDetails = new JsonObject();
                        skinDetails.addProperty("skin_name", rs.getString("skin_name"));
                        skinDetails.addProperty("win_num", rs.getInt("win_num"));
                        skinDetails.addProperty("vote_count", rs.getInt("vote_count"));
                        skinDetails.addProperty("win_rate", rs.getDouble("win_rate"));
                        skinDetails.addProperty("icon", rs.getString("icon"));

                        String jsonResponse = skinDetails.toString();
                        System.out.println("Sending response: " + jsonResponse);

                        try (PrintWriter out = response.getWriter()) {
                            out.write(jsonResponse);
                        }
                    } else {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "Skin not found: " + skinName);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        response.getWriter().write(error.toString());
    }
}