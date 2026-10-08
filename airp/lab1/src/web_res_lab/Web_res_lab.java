package web_res_lab;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

// Вариант 2: поиск названия лекарства по 1-3 симптомам.
// Список лекарств и симптомов находится в текстовом файле medicines.txt,
// описания лекарств (html-документы) находятся в папке source_html.
public class Web_res_lab extends Frame implements ActionListener {

    Button exitButton = new Button("Exit");
    Button searchButton = new Button("Search");
    TextArea textArea = new TextArea();

    public Web_res_lab() {
        super("My Window");
        setLayout(null);
        setBackground(new Color(150, 200, 100));
        setSize(450, 250);

        exitButton.setBounds(110, 190, 100, 20);
        exitButton.addActionListener(this);
        add(exitButton);

        searchButton.setBounds(110, 165, 100, 20);
        searchButton.addActionListener(this);
        add(searchButton);

        textArea.setBounds(20, 50, 300, 100);
        add(textArea);

        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == exitButton) {
            System.exit(0);
        } else if (ae.getSource() == searchButton) {
            // Ключевые слова (симптомы) разделяются запятыми
            String[] keywords = textArea.getText().split(",");
            textArea.setText("");

            // Список лекарств читается из текстового файла
            File medicinesFile = new File("medicines.txt");
            ArrayList<String> medicines = readLines(medicinesFile);

            String bestName = null;
            int bestCount = 0;
            for (String line : medicines) {
                // Формат строки: Название | симптом1, симптом2, ...
                String[] parts = line.split("\\|");
                if (parts.length < 2) {
                    continue;
                }
                String name = parts[0].trim();
                int coincidenceCount = testSymptoms(parts[1], keywords);
                textArea.append("\n" + name + "  :" + coincidenceCount);
                if (coincidenceCount > bestCount) {
                    bestCount = coincidenceCount;
                    bestName = name;
                }
            }

            if (bestName == null) {
                textArea.append("\nЛекарство не найдено");
            } else {
                textArea.append("\n\nЛекарство: " + bestName);
                openInBrowser(new File("source_html", bestName + ".html"));
            }
        }
    }

    // Чтение строк файла через URL и потоковые классы
    private ArrayList<String> readLines(File file) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            URL url = file.toURI().toURL();
            URLConnection connection = url.openConnection();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return lines;
    }

    // Число ключевых слов, найденных среди симптомов лекарства
    private int testSymptoms(String symptoms, String[] keywords) {
        int results = 0;
        String content = symptoms.toLowerCase();
        for (String keyword : keywords) {
            if (!keyword.trim().isEmpty() && content.contains(keyword.trim().toLowerCase())) {
                results++;
            }
        }
        return results;
    }

    // Открытие html-документа в браузере
    private void openInBrowser(File file) {
        try {
            Desktop.getDesktop().browse(file.getAbsoluteFile().toURI());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Web_res_lab();
    }
}
