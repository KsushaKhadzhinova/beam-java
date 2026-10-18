import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Вариант 2: сервлет возвращает на сторону клиента файл с рисунком
// ТЕОРИЯ: заголовок Content-Type (setContentType) сообщает браузеру тип ответа, поэтому
// "image/png" браузер показывает как рисунок, а не как текст. Бинарные данные пишутся в
// getOutputStream() (для текста используется getWriter()). Файл читается кусками через буфер.
public class ImageServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Файл с рисунком находится в каталоге приложения
        // открываем файл рисунка из папки приложения (webapp/images)
        InputStream in = getServletContext().getResourceAsStream("/images/picture.png");
        if (in == null) {  // файла нет -> ответ 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Файл рисунка не найден");
            return;
        }

        // Тип содержимого: браузер сам отображает рисунок
        response.setContentType("image/png");  // сообщаем браузеру тип данных
        OutputStream out = response.getOutputStream();
        // копируем файл в ответ кусками по 4 КБ
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();
    }

    @Override
    // GET и POST оба идут в processRequest
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
