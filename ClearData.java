import java.sql.*;
public class ClearData {
    public static void main(String[] args) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:sqlite:interview_mentor.db");
        Statement s = c.createStatement();
        s.executeUpdate("DELETE FROM quiz_results");
        s.executeUpdate("DELETE FROM quiz_sessions");
        s.executeUpdate("DELETE FROM daily_activity");
        s.executeUpdate("DELETE FROM bookmarks");
        try { s.executeUpdate("DELETE FROM achievements"); } catch (Exception ignored) {}
        System.out.println("All performance data, history, streaks, bookmarks, and achievements cleared!");
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM questions");
        System.out.println("Questions remaining: " + rs.getInt(1));
        s.close(); c.close();
    }
}
