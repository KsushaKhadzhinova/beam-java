import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Дополнительное задание: цена и картинка чизбургера с сайта mak.by
public class MakServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. читаем html страницы с бургерами по url
        URL url = new URL("https://mak.by/catalog/burgery/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        StringBuilder page = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            page.append(line);
        }
        in.close();
        String html = page.toString();

        // 2. ищем теги чизбургера: название, потом ближайшие за ним картинка и цена
        int name = html.indexOf("<div class=\"category__label\">Чизбургер</div>");
        int imgStart = html.lastIndexOf("<img src=\"", name) + 10;
        int imgEnd = html.indexOf("\"", imgStart);
        String image = "https://mak.by" + html.substring(imgStart, imgEnd);

        int priceStart = html.indexOf("<div class=\"category__price\">", name) + 29;
        int priceEnd = html.indexOf("</div>", priceStart);
        String price = html.substring(priceStart, priceEnd);

        // 3. выводим цену и картинку
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Чизбургер</title>");
        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        out.println("</head>");
        out.println("<body bgcolor='#aaccff'>");
        out.println("<h2>Чизбургер, цена: " + price + "</h2>");
        out.println("<img src=\"" + image + "\" width=\"300\">");
        out.println("<br><a href=\"dictionary.html\">Назад</a>");
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
