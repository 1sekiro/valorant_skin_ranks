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
                int[] selectedSkinIds = new int[2];
                int count = 0;

                while (skinRs.next() && count < 2) {
                    JsonObject skin = new JsonObject();
                    int skinId = skinRs.getInt("skin_id");
                    skin.addProperty("id", skinId);
                    skin.addProperty("name", skinRs.getString("skin_name"));
                    skin.addProperty("icon", skinRs.getString("icon"));
                    skins.add(skin);
                    selectedSkinIds[count++] = skinId;
                }

                if (skins.size() == 2) {
                    // Update vote_count for the selected skins
                    String updateVoteCountQuery = "UPDATE skin SET vote_count = vote_count + 1 WHERE skin_id IN (?, ?)";
                    PreparedStatement updateStmt = connection.prepareStatement(updateVoteCountQuery);
                    updateStmt.setInt(1, selectedSkinIds[0]);
                    updateStmt.setInt(2, selectedSkinIds[1]);
                    updateStmt.executeUpdate();

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
        int winningSkinId = jsonRequest.get("skinId").getAsInt();
        int losingSkinId = jsonRequest.get("otherSkinId").getAsInt();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try {
                // Update win count for the winning skin
                String updateVoteQuery = "UPDATE skin SET win_num = win_num + 1 WHERE skin_id = ?";
                PreparedStatement stmt = connection.prepareStatement(updateVoteQuery);
                stmt.setInt(1, winningSkinId);

                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    // Record the vote in vote_history
                    String insertHistoryQuery = "INSERT INTO vote_history (winning_skin_id, losing_skin_id) VALUES (?, ?)";
                    PreparedStatement historyStmt = connection.prepareStatement(insertHistoryQuery);
                    historyStmt.setInt(1, winningSkinId);
                    historyStmt.setInt(2, losingSkinId);
                    historyStmt.executeUpdate();

                    connection.commit(); // Commit transaction
                    response.setStatus(200);
                    JsonObject successResponse = new JsonObject();
                    successResponse.addProperty("message", "Vote recorded successfully.");
                    response.getWriter().write(successResponse.toString());
                } else {
                    connection.rollback();
                    response.setStatus(404);
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("error", "Skin ID not found. Vote not recorded.");
                    response.getWriter().write(errorResponse.toString());
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback on error
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            response.setStatus(500);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Database error: " + e.getMessage());
            response.getWriter().write(errorResponse.toString());
        }
    }}