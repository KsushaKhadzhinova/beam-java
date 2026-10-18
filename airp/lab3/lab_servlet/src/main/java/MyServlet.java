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
//
// ТЕОРИЯ, которую спрашивает преподаватель (контрольные вопросы и лекции):
// - Сервлет - Java-класс на стороне сервера (наследник HttpServlet), работает внутри
//   контейнера (Tomcat). Жизненный цикл: init() один раз -> doGet/doPost на каждый запрос ->
//   destroy() при остановке. Основные объекты: HttpServletRequest (запрос) и HttpServletResponse (ответ).
// - Как сервлет вызывается с сайта: <form action="MyServlet" method="Get">; поле name="txt"
//   читается как request.getParameter("txt"). GET - параметры в URL, POST - в теле запроса.
// - web.xml (WEB-INF): <servlet> задаёт имя и класс, <servlet-mapping> - URL, <welcome-file-list> -
//   стартовую страницу, <session-timeout> - время жизни сессии (минуты).
// - Результат клиенту: response.getWriter() + println (текст/html) или getOutputStream() (байты).
// - Tomcat - веб-сервер и сервлет-контейнер (порт 8080); приложение упаковывается в WAR.
// - JDBC: PreparedStatement с "?" защищает от SQL-инъекций; H2 - встроенная база данных.
// Переводы хранятся в базе данных.
public class MyServlet extends HttpServlet {

    private Connection connection;  // соединение с базой данных

    // Создание базы данных с переводами слов
    @Override
    public void init() throws ServletException {  // вызывается один раз при загрузке сервлета
        try {
            Class.forName("org.h2.Driver");
            // база H2 в памяти: исчезает при остановке сервера
            connection = DriverManager.getConnection("jdbc:h2:mem:dictionary;DB_CLOSE_DELAY=-1");
            Statement st = connection.createStatement();
            st.execute("CREATE TABLE words (rus VARCHAR(50), eng VARCHAR(50))");
            // заполняем таблицу переводами
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
        // ? - параметр запроса (защита от SQL-инъекций)
        PreparedStatement ps = connection.prepareStatement(
                "SELECT " + to + " FROM words WHERE " + from + " = ?");
        ps.setString(1, word.trim().toLowerCase());
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString(1) : null;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();  // поток для записи html-ответа

        String rus_word = request.getParameter("txt"); // русское слово
        String eng_word = request.getParameter("eng"); // английское слово (обратный перевод)

        String result;  // текст, который покажем на странице
        try {
            // есть русское слово -> перевод; есть английское -> обратный перевод
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

        // формируем html-страницу ответа
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
