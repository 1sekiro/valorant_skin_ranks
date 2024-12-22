import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class updateDatabase {

    public static void main(String[] args) {
        String newJsonFilePath = "C:\\Users\\yikez\\IdeaProjects\\valorant-skin-ranks\\new_skins.json";

        String dbUrl = "jdbc:mysql://172.26.144.22:3306/skindb";
        String dbUser = "root";
        String dbPassword = "mypassword";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            processJsonFile(newJsonFilePath, connection);
            System.out.println("Data insertion complete.");
        } catch (IOException e) {
            System.err.println("Failed to read the JSON file: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void processJsonFile(String jsonFilePath, Connection connection) throws IOException, SQLException {
        String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        JSONArray skinsArray = new JSONArray(content);

        for (int i = 0; i < skinsArray.length(); i++) {
            JSONObject skinNode = skinsArray.getJSONObject(i);
            String skinName = skinNode.optString("name", "");
            String weaponName = skinNode.optString("weapon", null);
            String imageUrl = skinNode.optString("image_url", null);

            if (weaponName == null) {
                System.out.println("Skipping incomplete skin entry: " + skinName);
                continue;
            }

            int weaponId = fetchWeaponId(connection, weaponName);

            // If no image URL is present, check for variants
            if (imageUrl == null) {
                JSONArray variants = skinNode.optJSONArray("variants");
                if (variants == null || variants.length() == 0) {
                    System.out.println("Skipping incomplete skin entry: " + skinName);
                    continue;
                }

                for (int j = 0; j < variants.length(); j++) {
                    JSONObject variant = variants.getJSONObject(j);
                    String variantColor = variant.optString("color", "Default");
                    String variantImageUrl = variant.optString("image_url", null);

                    if (variantImageUrl != null) {
                        String variantName = skinName + " (" + variantColor + ")";
                        insertSkin(connection, variantName, weaponId, variantImageUrl);
                    }
                }
            } else {
                // Insert the main skin entry if it has an image URL
                insertSkin(connection, skinName, weaponId, imageUrl);
            }
        }
    }

    private static void insertSkin(Connection connection, String skinName, int weaponId, String imageUrl) throws SQLException {
        String skinInsertQuery = "INSERT INTO skin (skin_name, weapon_id, icon) VALUES (?, ?, ?)";
        try (PreparedStatement skinStmt = connection.prepareStatement(skinInsertQuery)) {
            skinStmt.setString(1, skinName);
            skinStmt.setInt(2, weaponId);
            skinStmt.setString(3, imageUrl);
            skinStmt.executeUpdate();
        }
    }


    private static int fetchWeaponId(Connection connection, String weaponName) throws SQLException {
        String query = "SELECT weapon_id FROM weapon WHERE weapon_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, weaponName);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("weapon_id");
                } else {
                    throw new SQLException("Weapon not found: " + weaponName);
                }
            }
        }
    }
}
