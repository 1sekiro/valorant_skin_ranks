import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class UpdateDatabase {

    // Database
    private static final String DB_URL = "jdbc:mysql://172.26.144.22:3306/skindb";
    //private static final String DB_URL = "jdbc:mysql://localhost:3306/skindb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mypassword";

    public static void main(String[] args) {
        Map<String, String> manualSkins = new HashMap<>();
        manualSkins.put("Altitude Odin", "https://media.valorant-api.com/weaponskinlevels/578e9077-4f88-260c-e54c-b988425c60e4/displayicon.png");
        manualSkins.put("Nitro Odin", "https://valorantstrike.com/wp-content/uploads/2021/09/Valorant-Nitro-Collection-Odin-HD.jpg");
        manualSkins.put("Snowfall Ares", "https://valorantstrike.com/wp-content/uploads/Valorant-Snowfall-Collection-Ares-HD.jpg");
        manualSkins.put("Aristocrat Vandal", "https://valorantstrike.com/wp-content/uploads/2020/06/Valorant-Aristocrat-Vandal.jpg");
        manualSkins.put("Nitro Vandal", "https://static.wikia.nocookie.net/valorant/images/a/a0/Nitro_Vandal.png/revision/latest?cb=20230711201647");
        manualSkins.put("Evori Dreamwings Vandal", "https://static.wikia.nocookie.net/valorant/images/4/42/Evori_Dreamwings_Vandal.png/revision/latest?cb=20240625161616");
        manualSkins.put("Aristocrat Bulldog", "https://valorantskins.com/img/skins/bulldog/aristocrat-bulldog-skin.png");
        manualSkins.put("Genesis Bulldog", "https://valorantstrike.com/wp-content/uploads/Valorant-Genesis-Collection-Bulldog-HD.jpg");
        manualSkins.put("Rush Phantom", "https://vgraphs.com/images/weapons/skins/full-details/valorant-rush-phantom-weapon-skin.png");
        manualSkins.put("Kingdom Phantom", "https://valorantskins.com/img/skins/phantom/kingdom-phantom-skin.png");
        manualSkins.put("Galleria Phantom", "https://valorantskins.com/img/skins/phantom/galleria-phantom-skin.png");
        manualSkins.put("Artisan Phantom", "https://valorantstrike.com/wp-content/uploads/2021/09/Valorant-Artisan-Collection-Phantom-HD.jpg");
        manualSkins.put("Snowfall Phantom", "https://valorantstrike.com/wp-content/uploads/Valorant-Snowfall-Collection-Phantom-HD.jpg");
        manualSkins.put("Rush Judge", "https://vgraphs.com/images/weapons/skins/full-details/valorant-rush-judge-weapon-skin.png");
        manualSkins.put(".EXE Judge", "https://valorantstrike.com/wp-content/uploads/2020/06/Valorant-Dot-Exe-Judge.jpg");
        manualSkins.put("Snowfall Judge", "https://static.wikia.nocookie.net/valorant/images/7/76/Snowfall_Judge.png/revision/latest?cb=20230711204831");
        manualSkins.put("Galleria Bucky", "https://vgraphs.com/images/weapons/skins/full-details/valorant-galleria-bucky-weapon-skin.png");
        manualSkins.put("Genesis Bucky", "https://valorantstrike.com/wp-content/uploads/Valorant-Genesis-Collection-Bucky-HD.jpg");
        manualSkins.put("Artisan Bucky", "https://valorantstrike.com/wp-content/uploads/2021/09/Valorant-Artisan-Collection-Bucky-HD.jpg");
        manualSkins.put("Rush Frenzy", "https://valorantskins.com/img/skins/frenzy/rush-frenzy-skin.png");
        manualSkins.put("Couture Frenzy", "https://valorantskins.com/img/skins/frenzy/couture-frenzy-skin.png");
        manualSkins.put("Spitfire Frenzy", "https://valorantskins.com/img/skins/frenzy/spitfire-frenzy-skin.png");
        manualSkins.put("Kingdom Classic", "https://valorantskins.com/img/skins/classic/kingdom-classic-skin.png");
        manualSkins.put("Snowfall Classic", "https://static.wikia.nocookie.net/valorant/images/e/ee/Snowfall_Classic.png/revision/latest?cb=20230711204826");
        manualSkins.put("Final Chamber Classic", "https://valorantskins.com/img/skins/classic/final-chamber-classic-skin.png");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            for (Map.Entry<String, String> entry : manualSkins.entrySet()) {
                String skinName = entry.getKey();
                String iconUrl = entry.getValue();

                String updateQuery = "UPDATE skin SET icon = ? WHERE skin_name = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                    statement.setString(1, iconUrl);
                    statement.setString(2, skinName);
                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Updated: " + skinName);
                    } else {
                        System.out.println("No matching record for: " + skinName);
                    }
                }
            }

            System.out.println("Manual updates complete.");
        } catch (Exception e) {
            System.err.println("Error updating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

