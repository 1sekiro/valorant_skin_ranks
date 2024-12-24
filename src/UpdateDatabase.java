import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.json.JSONArray;
import org.json.JSONObject;

public class UpdateDatabase {

    private static final String DB_URL = "jdbc:mysql://172.26.144.22:3306/skindb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mypassword";

    public static void main(String[] args) {
        String jsonFilePath = "C:\\Users\\yikez\\IdeaProjects\\valorant-skin-ranks\\new_skins.json"; //

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            updateIcons(jsonFilePath, connection);
            System.out.println("Database icon updates complete.");
        } catch (Exception e) {
            System.err.println("Error updating database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateIcons(String jsonFilePath, Connection connection) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        JSONArray skinsArray = new JSONArray(content);

        for (int i = 0; i < skinsArray.length(); i++) {
            JSONObject skinNode = skinsArray.getJSONObject(i);
            String skinName = skinNode.getString("name");
            String iconUrl = skinNode.optString("image_url", null);

            if (iconUrl == null) {
                System.out.println("Skipping skin with no icon: " + skinName);
                continue;
            }

            String updateQuery = "UPDATE skin SET icon = ? WHERE skin_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                statement.setString(1, iconUrl);
                statement.setString(2, skinName);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Updated icon for: " + skinName);
                } else {
                    System.out.println("No matching record for: " + skinName);
                }
            } catch (Exception e) {
                System.err.println("Error updating icon for " + skinName + ": " + e.getMessage());
            }
        }
    }
}

