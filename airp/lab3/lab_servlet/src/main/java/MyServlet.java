import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyServlet extends HttpServlet {

    private Connection connection;  // соединение с базой данных

    // создаём базу данных с переводами (один раз, при загрузке сервлета)
    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:dictionary;DB_CLOSE_DELAY=-1");
            Statement st = connection.createStatement();
            st.execute("CREATE TABLE words (rus VARCHAR(50), eng VARCHAR(50))");
            st.execute("INSERT INTO words VALUES ('привет', 'hello'), ('дом', 'house'), "
                    + "('книга', 'book'), ('студент', 'student'), ('компьютер', 'computer')");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // from - столбец, в котором ищем слово, to - столбец с переводом
    private String translate(String from, String to, String word) throws Exception {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT " + to + " FROM words WHERE " + from + " = ?");
        ps.setString(1, word.trim().toLowerCase());
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString(1) : "не найдено";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String rus_word = request.getParameter("txt");  // русское слово
        String eng_word = request.getParameter("eng");  // английское слово
        String result = "Слово не введено";
        try {
            if (rus_word != null && !rus_word.trim().isEmpty()) {
                result = rus_word + " - " + translate("rus", "eng", rus_word);
            } else if (eng_word != null && !eng_word.trim().isEmpty()) {
                result = eng_word + " - " + translate("eng", "rus", eng_word);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet MyServlet</title>");
        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        out.println("</head>");
        out.println("<body bgcolor='#aaccff'>");
        out.println("<h2>" + result + "</h2><br><br>");
        out.println("<a href=\"dictionary.html\">Назад</a>");
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
