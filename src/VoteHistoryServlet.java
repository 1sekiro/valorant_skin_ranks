import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "VoteHistoryServlet", urlPatterns = "/api/vote-history")
public class VoteHistoryServlet extends HttpServlet {
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
        String skinName = request.getParameter("skinName");

        if (skinName == null || skinName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing skinName parameter\"}");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String query =
                    "SELECT " +
                            "    vh.vote_id, " +
                            "    w.skin_name as winner_name, " +
                            "    l.skin_name as loser_name, " +
                            "    w.icon as winner_icon, " +
                            "    l.icon as loser_icon, " +
                            "    CASE WHEN w.skin_name = ? THEN 'won' ELSE 'lost' END as result, " +
                            "    (" +
                            "        SELECT COUNT(*) " +
                            "        FROM vote_history vh2 " +
                            "        WHERE (vh2.winning_skin_id = vh.winning_skin_id AND vh2.losing_skin_id = vh.losing_skin_id) " +
                            "           OR (vh2.winning_skin_id = vh.losing_skin_id AND vh2.losing_skin_id = vh.winning_skin_id)" +
                            "    ) as total_matches, " +
                            "    (" +
                            "        SELECT COUNT(*) " +
                            "        FROM vote_history vh2 " +
                            "        WHERE vh2.winning_skin_id = vh.winning_skin_id " +
                            "        AND vh2.losing_skin_id = vh.losing_skin_id" +
                            "    ) as direct_wins " +
                            "FROM vote_history vh " +
                            "JOIN skin w ON vh.winning_skin_id = w.skin_id " +
                            "JOIN skin l ON vh.losing_skin_id = l.skin_id " +
                            "WHERE w.skin_name = ? OR l.skin_name = ? " +
                            "ORDER BY vh.vote_id DESC " +
                            "LIMIT 10";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, skinName);
            stmt.setString(2, skinName);
            stmt.setString(3, skinName);

            ResultSet rs = stmt.executeQuery();
            JsonArray history = new JsonArray();

            while (rs.next()) {
                JsonObject match = new JsonObject();
                String winnerName = rs.getString("winner_name");
                String loserName = rs.getString("loser_name");
                int totalMatches = rs.getInt("total_matches");
                int directWins = rs.getInt("direct_wins");

                // Calculate win rates
                double winnerWinRate = (totalMatches > 0) ? (directWins * 100.0 / totalMatches) : 0.0;
                double loserWinRate = (totalMatches > 0) ? ((totalMatches - directWins) * 100.0 / totalMatches) : 0.0;

                match.addProperty("winner_name", winnerName);
                match.addProperty("loser_name", loserName);
                match.addProperty("winner_icon", rs.getString("winner_icon"));
                match.addProperty("loser_icon", rs.getString("loser_icon"));
                match.addProperty("result", rs.getString("result"));
                match.addProperty("total_matches", totalMatches);
                match.addProperty("win_rate", skinName.equals(winnerName) ? winnerWinRate : loserWinRate);

                history.add(match);
            }

            JsonObject response_data = new JsonObject();
            response_data.add("history", history);
            response.getWriter().write(response_data.toString());

        } catch (SQLException e) {
            System.err.println("Database error in VoteHistoryServlet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}