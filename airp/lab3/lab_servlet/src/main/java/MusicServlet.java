import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Сервлет возвращает на сторону клиента музыкальный файл (браузер сам проигрывает его)
public class MusicServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Файл с музыкой находится в каталоге приложения (webapp/music)
        InputStream in = getServletContext().getResourceAsStream("/music/music.wav");
        if (in == null) {  // файла нет -> ответ 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Музыкальный файл не найден");
            return;
        }

        // Тип содержимого: audio/wav - браузер запускает встроенный проигрыватель
        response.setContentType("audio/wav");
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

    // GET и POST оба идут в processRequest
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
