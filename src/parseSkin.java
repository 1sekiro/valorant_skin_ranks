import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class parseSkin {

    public static void main(String[] args) {
        //String jsonFilePath = "C:\\Users\\yikez\\IdeaProjects\\valorant-skin-ranks\\weapons.json";
        String jsonFilePath = "/Users/mingkunliu/Downloads/valorant-skin-ranks/skins.json";

        //String dbUrl = "jdbc:mysql://172.26.144.22:3306/skindb";
        String dbUrl = "jdbc:mysql://localhost:3306/skindb";
        String dbUser = "root";
        String dbPassword = "mypassword";

        Map<String, String> weaponMappings = new HashMap<>();
        weaponMappings.put("classic", "classic");
        weaponMappings.put("shorty", "shorty");
        weaponMappings.put("frenzy", "frenzy");
        weaponMappings.put("ghost", "ghost");
        weaponMappings.put("sheriff", "sheriff");
        weaponMappings.put("stinger", "stinger");
        weaponMappings.put("spectre", "spectre");
        weaponMappings.put("bucky", "bucky");
        weaponMappings.put("judge", "judge");
        weaponMappings.put("bulldog", "bulldog");
        weaponMappings.put("guardian", "guardian");
        weaponMappings.put("phantom", "phantom");
        weaponMappings.put("vandal", "vandal");
        weaponMappings.put("melee", "melee");
        weaponMappings.put("outlaw", "outlaw");
        weaponMappings.put("marshal", "marshal");
        weaponMappings.put("operator", "operator");
        weaponMappings.put("ares", "ares");
        weaponMappings.put("odin", "odin");

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Fetch the weapon ID for melee
            int meleeWeaponId = fetchWeaponId(connection, "melee");

            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JSONObject root = new JSONObject(content);

            JSONArray skinsArray = root.getJSONArray("data");
            for (int i = 0; i < skinsArray.length(); i++) {
                JSONObject skinNode = skinsArray.getJSONObject(i);
                String skinName = skinNode.getString("displayName");

                // Skip "Standard" and "Random Favorite Skin"
                if (skinName.toLowerCase().contains("standard") || skinName.toLowerCase().contains("favorite random skin")) {
                    continue;
                }

                // Identify weapon name using mappings
                String weaponName = extractWeaponName(skinName, weaponMappings);

                // If weapon name is not found, assume it's a melee type
                int weaponId = (weaponName != null) ? fetchWeaponId(connection, weaponName) : meleeWeaponId;

                JSONArray chromasArray = skinNode.optJSONArray("chromas");
                if (chromasArray != null) {
                    for (int j = 0; j < chromasArray.length(); j++) {
                        JSONObject chromaNode = chromasArray.getJSONObject(j);
                        String chromaName = chromaNode.getString("displayName");
                        String chromaIcon = chromaNode.optString("displayIcon", null);

                        // Clean up chroma name to remove "(Variant X Color)"
                        String cleanedChromaName = cleanChromaName(chromaName);

                        // Insert chroma as a skin into the Skin table
                        String skinInsertQuery = "INSERT INTO skin (skin_name, weapon_id, icon) VALUES (?, ?, ?)";
                        try (PreparedStatement skinStmt = connection.prepareStatement(skinInsertQuery)) {
                            skinStmt.setString(1, cleanedChromaName);
                            skinStmt.setInt(2, weaponId);
                            skinStmt.setString(3, chromaIcon);
                            skinStmt.executeUpdate();
                        }
                    }
                }
            }

            System.out.println("Data insertion complete.");
            deleteStandardSkins(connection);
        } catch (IOException e) {
            System.err.println("Failed to read the JSON file: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static String extractWeaponName(String skinName, Map<String, String> weaponMappings) {
        for (Map.Entry<String, String> entry : weaponMappings.entrySet()) {
            if (skinName.toLowerCase().contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null; // Return null for unrecognized weapons
    }

    private static String cleanChromaName(String chromaName) {
        if (chromaName.contains("(Variant")) {
            return chromaName.replaceAll("\\(Variant \\d+ (.*?)\\)", "($1)").trim();
        }
        return chromaName.trim();
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
    private static void deleteStandardSkins(Connection connection) throws SQLException {
        String deleteQuery = "DELETE FROM skin WHERE LOWER(skin_name) LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setString(1, "%standard%");
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " skins containing 'standard' in their name.");
        }
    }
}