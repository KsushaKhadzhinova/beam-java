import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

// Вариант 2: два клиента, каждый в своём потоке.
// Клиент 1 добавляет и снимает деньги со счёта, клиент 2 только снимает.

// Серверный поток: хранит счёт и обрабатывает запросы клиентов
class Account extends Thread {
    ServerSocket server;
    String amountstring;
    static int amount = 200;

    public void run() {
        try {
            server = new ServerSocket(3001); // Номер сокета
        } catch (Exception e) {
            System.out.println("Ошибка соединения+" + e);
        }
        while (true) {
            Socket s = null;
            try {
                s = server.accept(); // ожидание соединения с клиентом
            } catch (Exception e) {
                System.out.println("Ошибка" + e);
            }
            try {
                // Чтение запроса клиента (ADD или WITHDRAW)
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String request = in.readLine();

                PrintStream ps = new PrintStream(s.getOutputStream()); // PrintStream предназначен для текстового вывода
                int amountcur = ((int) (Math.random() * 1000));
                if (request.equals("ADD"))
                    amount += amountcur;
                else
                    amount -= amountcur;
                amountstring = Integer.toString(amount);
                ps.println("Account:" + amountstring); // передача строки клиенту
                ps.flush();
                s.close(); // Сокетное соединение закрывается
            } catch (Exception e) {
                System.out.println("Ошибка  " + e);
            }
        }
    }
}

// Клиентский поток: отправляет запрос серверу и читает ответ
class clientThread extends Thread {
    DataInputStream dis = null;
    Socket s = null;
    String name;
    String request;
    serv window;

    public clientThread(String name, String request, serv window) {
        this.name = name;
        this.request = request;
        this.window = window;
        try {
            s = new Socket("127.0.0.1", 3001);
            dis = new DataInputStream(s.getInputStream());
        } catch (Exception e) {
            System.out.println("Ошибка:  " + e);
        }
    }

    public void run() {
        try {
            // Отправка запроса серверу
            PrintStream ps = new PrintStream(s.getOutputStream());
            ps.println(request);
            ps.flush();

            String msg = dis.readLine(); // Клиент читает строку из сокета
            if (msg != null) {
                System.out.println(name + ": " + msg);
                window.showMessage(name + ": " + msg);
            }
            s.close();
        } catch (Exception e) {
            System.out.println("ERRORR+" + e);
        }
    }
}

// Окно приложения
public class serv extends Frame implements ActionListener {
    Button client1Add = new Button("Клиент 1: добавить");
    Button client1Withdraw = new Button("Клиент 1: снять");
    Button client2Withdraw = new Button("Клиент 2: снять");
    TextField accountField = new TextField("200", 10);
    TextArea log = new TextArea(8, 40);

    public serv() {
        super("Счёт");
        setLayout(new FlowLayout());
        add(client1Add);
        add(client1Withdraw);
        add(client2Withdraw);
        add(new Label("Счёт:"));
        add(accountField);
        add(log);

        client1Add.addActionListener(this);
        client1Withdraw.addActionListener(this);
        client2Withdraw.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0); // Закрыть приложение
            }
        });
    }

    // Запуск клиента по нажатию кнопки
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == client1Add)
            new clientThread("Клиент 1", "ADD", this).start();
        else if (ae.getSource() == client1Withdraw)
            new clientThread("Клиент 1", "WITHDRAW", this).start();
        else if (ae.getSource() == client2Withdraw)
            new clientThread("Клиент 2", "WITHDRAW", this).start();
    }

    // Отображение состояния счёта
    public void showMessage(String message) {
        accountField.setText(message.substring(message.indexOf(':', message.indexOf(':') + 1) + 1));
        log.append(message + "\n");
    }

    public static void main(String args[]) {
        serv f = new serv();
        f.setSize(400, 400);
        f.setVisible(true);
        new Account().start();
    }
}
