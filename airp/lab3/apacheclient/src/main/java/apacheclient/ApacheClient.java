package apacheclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;

// Клиент словаря: вызывает сервлет по url из обычного java-приложения
// ТЕОРИЯ: клиент и сервер - два разных проекта; сервлет - это сервис по URL, его можно
// вызвать не только из браузера, но и из любой программы по протоколу HTTP (GET-запрос).
// HttpClient (Apache) отправляет запрос, EntityUtils читает тело ответа (html); параметры
// кодируются URLEncoder, чтобы русские буквы корректно ушли в адресе.
public class ApacheClient {

    // адрес сервлета на сервере (Tomcat, порт 8080)
    static String url = "http://localhost:8080/lab_servlet/MyServlet";

    // Отправка GET-запроса сервлету и выделение результата из html-ответа
    static String ask(String parameter, String word) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // GET-запрос вида ...MyServlet?txt=слово
            HttpGet request = new HttpGet(url + "?" + parameter + "=" + URLEncoder.encode(word, "UTF-8"));
            HttpResponse response = client.execute(request);  // отправка запроса серверу
            String html = EntityUtils.toString(response.getEntity(), "UTF-8");
            // из html-ответа берём текст между <h2> и </h2>
            int start = html.indexOf("<h2>") + 4;
            int end = html.indexOf("</h2>");
            return html.substring(start, end);
        }
    }

    public static void main(String[] args) {
        // окно клиента: поле, две кнопки и надпись с результатом
        JFrame frame = new JFrame("Словарь (клиент)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JTextField wordField = new JTextField(15);
        JButton translateButton = new JButton("Перевести");
        JButton reverseButton = new JButton("Перевести обратно");
        JLabel resultLabel = new JLabel("Введите слово");

        translateButton.addActionListener(e -> {  // рус -> англ (параметр txt)
            try {
                resultLabel.setText(ask("txt", wordField.getText()));
            } catch (Exception ex) {
                resultLabel.setText("Ошибка: " + ex.getMessage());
            }
        });
        reverseButton.addActionListener(e -> {  // англ -> рус (параметр eng)
            try {
                resultLabel.setText(ask("eng", wordField.getText()));
            } catch (Exception ex) {
                resultLabel.setText("Ошибка: " + ex.getMessage());
            }
        });

        frame.add(new JLabel("Слово:"));
        frame.add(wordField);
        frame.add(translateButton);
        frame.add(reverseButton);
        frame.add(resultLabel);
        frame.setSize(520, 120);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
