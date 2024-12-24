import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import com.google.gson.JsonParser;

@WebServlet(name = "VoteServlet", urlPatterns = "/api/vote")
public class VoteServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/skindb");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String selectedWeapon = request.getParameter("weapon"); // Optional filter by weapon

        try (Connection connection = dataSource.getConnection()) {
            String weaponQuery;
            String skinQuery;

            if (selectedWeapon != null && !selectedWeapon.isEmpty()) {
                // Use the selected weapon to filter skins
                weaponQuery = "SELECT weapon_id FROM weapon WHERE weapon_name = ?";
                skinQuery = "SELECT skin_id, skin_name, icon FROM skin WHERE weapon_id = ? ORDER BY RAND() LIMIT 2";
            } else {
                // Randomly select a weapon and then fetch two skins for it
                weaponQuery = "SELECT weapon_id FROM weapon ORDER BY RAND() LIMIT 1";
                skinQuery = "SELECT skin_id, skin_name, icon FROM skin WHERE weapon_id = ? ORDER BY RAND() LIMIT 2";
            }

            // Get weapon ID
            PreparedStatement weaponStmt = connection.prepareStatement(weaponQuery);
            if (selectedWeapon != null && !selectedWeapon.isEmpty()) {
                weaponStmt.setString(1, selectedWeapon);
            }
            ResultSet weaponRs = weaponStmt.executeQuery();

            if (weaponRs.next()) {
                int weaponId = weaponRs.getInt("weapon_id");

                // Get two random skins for the selected weapon
                PreparedStatement skinStmt = connection.prepareStatement(skinQuery);
                skinStmt.setInt(1, weaponId);
                ResultSet skinRs = skinStmt.executeQuery();

                JsonArray skins = new JsonArray();
                while (skinRs.next()) {
                    JsonObject skin = new JsonObject();
                    skin.addProperty("id", skinRs.getInt("skin_id"));
                    skin.addProperty("name", skinRs.getString("skin_name"));
                    skin.addProperty("icon", skinRs.getString("icon"));
                    skins.add(skin);
                }

                if (skins.size() == 2) {
                    try (PrintWriter out = response.getWriter()) {
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.add("skin1", skins.get(0));
                        jsonResponse.add("skin2", skins.get(1));
                        out.write(jsonResponse.toString());
                    }
                } else {
                    response.setStatus(500);
                    response.getWriter().write("{\"error\": \"Not enough skins to compare for the selected weapon.\"}");
                }
                skinRs.close();
                skinStmt.close();
            } else {
                response.setStatus(500);
                response.getWriter().write("{\"error\": \"No weapons found.\"}");
            }

            weaponRs.close();
            weaponStmt.close();
        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject jsonRequest = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        int skinId = jsonRequest.get("skinId").getAsInt();

        try (Connection connection = dataSource.getConnection()) {
            String updateVoteQuery = "UPDATE skin SET win_num = win_num + 1 WHERE skin_id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateVoteQuery);
            stmt.setInt(1, skinId);

            int rowsUpdated = stmt.executeUpdate(); // Track rows updated

            // Confirm update success
            if (rowsUpdated > 0) {
                response.setStatus(200);
                try (PrintWriter out = response.getWriter()) {
                    JsonObject successResponse = new JsonObject();
                    successResponse.addProperty("message", "Vote recorded successfully.");
                    out.write(successResponse.toString());
                }
            } else {
                response.setStatus(404); // Skin ID not found
                try (PrintWriter out = response.getWriter()) {
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("error", "Skin ID not found. Vote not recorded.");
                    out.write(errorResponse.toString());
                }
            }
        } catch (SQLException e) {
            response.setStatus(500);
            try (PrintWriter out = response.getWriter()) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Database error: " + e.getMessage());
                out.write(errorResponse.toString());
            }
        }
    }

}
