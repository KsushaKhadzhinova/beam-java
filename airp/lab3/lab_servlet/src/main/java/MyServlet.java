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

// Сервлет словаря: перевод с русского на английский и обратный перевод.
// Переводы хранятся в базе данных.
public class MyServlet extends HttpServlet {

    private Connection connection;

    // Создание базы данных с переводами слов
    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:dictionary;DB_CLOSE_DELAY=-1");
            Statement st = connection.createStatement();
            st.execute("CREATE TABLE words (rus VARCHAR(50), eng VARCHAR(50))");
            st.execute("INSERT INTO words VALUES ('привет', 'hello'), ('дом', 'house'), "
                    + "('книга', 'book'), ('студент', 'student'), ('компьютер', 'computer'), "
                    + "('вода', 'water'), ('солнце', 'sun'), ('друг', 'friend'), "
                    + "('школа', 'school'), ('город', 'city')");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // Поиск перевода в базе данных: from - столбец с исходным словом, to - столбец с переводом
    private String translate(String from, String to, String word) throws Exception {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT " + to + " FROM words WHERE " + from + " = ?");
        ps.setString(1, word.trim().toLowerCase());
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString(1) : null;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String rus_word = request.getParameter("txt"); // русское слово
        String eng_word = request.getParameter("eng"); // английское слово (обратный перевод)

        String result;
        try {
            if (rus_word != null && !rus_word.trim().isEmpty()) {
                String found = translate("rus", "eng", rus_word);
                result = found == null ? "Слово «" + rus_word + "» не найдено" : rus_word + " - " + found;
            } else if (eng_word != null && !eng_word.trim().isEmpty()) {
                String found = translate("eng", "rus", eng_word);
                result = found == null ? "Слово «" + eng_word + "» не найдено" : eng_word + " - " + found;
            } else {
                result = "Слово не введено";
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
