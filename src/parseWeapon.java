import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class parseWeapon {

    public static void main(String[] args) {
        // Path to the JSON file
        String jsonFilePath = "/Users/mingkunliu/Downloads/valorant-skin-ranks/weapons.json";

        // Database connection details
        String dbUrl = "jdbc:mysql://localhost:3306/skindb";
        String dbUser = "root";
        String dbPassword = "mypassword";

        Set<String> validWeapons = new HashSet<>();
        validWeapons.add("classic");
        validWeapons.add("shorty");
        validWeapons.add("frenzy");
        validWeapons.add("ghost");
        validWeapons.add("sheriff"); // Sidearms
        validWeapons.add("stinger");
        validWeapons.add("spectre"); // SMGs
        validWeapons.add("bucky");
        validWeapons.add("judge"); // Shotguns
        validWeapons.add("bulldog");
        validWeapons.add("guardian");
        validWeapons.add("phantom");
        validWeapons.add("vandal"); // Rifles
        validWeapons.add("melee"); // Melee
        validWeapons.add("marshal");
        validWeapons.add("outlaw");
        validWeapons.add("operator"); // Sniper Rifles
        validWeapons.add("ares");
        validWeapons.add("odin"); // Machine Guns

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            System.out.println("Connected to the database.");

            // Parse the JSON file
            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JSONObject root = new JSONObject(content);

            // Iterate over the weapons
            JSONArray weaponsArray = root.getJSONArray("data");
            for (int i = 0; i < weaponsArray.length(); i++) {
                JSONObject weaponNode = weaponsArray.getJSONObject(i);
                String weaponName = weaponNode.getString("displayName").toLowerCase();

                // Skip invalid weapons
                if (!validWeapons.contains(weaponName)) {
                    continue;
                }

                String weaponIcon = weaponNode.optString("displayIcon", null);

                // Insert weapon into the Weapon table
                String weaponInsertQuery = "INSERT IGNORE INTO weapon (weapon_name) VALUES (?)";
                try (PreparedStatement weaponStmt = connection.prepareStatement(weaponInsertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    weaponStmt.setString(1, weaponName);
                    weaponStmt.executeUpdate();

                    // Get the generated weapon_id
                    int weaponId;
                    try (var rs = weaponStmt.getGeneratedKeys()) {
                        rs.next();
                        weaponId = rs.getInt(1);
                    }

                    // Process skins for the weapon
                    JSONArray skinsArray = weaponNode.optJSONArray("skins");
                    if (skinsArray != null) {
                        for (int j = 0; j < skinsArray.length(); j++) {
                            JSONObject skinNode = skinsArray.getJSONObject(j);
                            String skinName = skinNode.getString("displayName");
                            String skinIcon = skinNode.optString("displayIcon", null);

                            // Insert skin into the Skin table
                            String skinInsertQuery = "INSERT INTO skin (skin_name, weapon_id, icon) VALUES (?, ?, ?)";
                            try (PreparedStatement skinStmt = connection.prepareStatement(skinInsertQuery)) {
                                skinStmt.setString(1, skinName);
                                skinStmt.setInt(2, weaponId);
                                skinStmt.setString(3, skinIcon);
                                skinStmt.executeUpdate();
                            }
                        }
                    }
                }
            }

            System.out.println("Data insertion complete.");
        } catch (IOException e) {
            System.err.println("Failed to read the JSON file: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
