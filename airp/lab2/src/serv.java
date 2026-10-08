import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Enumeration;

// Вариант 2: два клиента, каждый в своём потоке.
// Клиент 1 добавляет и снимает деньги со счёта, клиент 2 только снимает.
//
// Запуск:
//   java -jar serv.jar             - сервер и клиенты на одном компьютере (адрес 127.0.0.1)
//   java -jar serv.jar 192.168.0.5 - только клиенты, сервер работает на компьютере с указанным адресом

// Серверный поток: хранит счёт и обрабатывает запросы клиентов
class Account extends Thread {
    ServerSocket server;
    String amountstring;
    static int amount = 200;
    serv window; // окно, в журнал которого сервер записывает запросы

    public Account(serv window) {
        this.window = window;
    }

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
                // Запись в журнал сервера: откуда пришёл запрос и каким стал счёт
                window.log.append("[сервер] " + request + " от " + s.getInetAddress().getHostAddress() + ", счёт: " + amountstring + "\n");
                window.accountField.setText(amountstring);
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
    String host;
    serv window;

    public clientThread(String name, String request, serv window, String host) {
        this.name = name;
        this.request = request;
        this.window = window;
        this.host = host;
        try {
            s = new Socket(host, 3001);
            dis = new DataInputStream(s.getInputStream());
        } catch (Exception e) {
            System.out.println("Ошибка:  " + e);
        }
    }

    public void run() {
        if (s == null) { // соединение не установлено
            window.log.append(name + ": нет связи с сервером " + host + "\n");
            return;
        }
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
    String host; // адрес компьютера, на котором работает сервер

    public serv() {
        this("127.0.0.1");
    }

    public serv(String host) {
        super("Счёт");
        this.host = host;
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
            new clientThread("Клиент 1", "ADD", this, host).start();
        else if (ae.getSource() == client1Withdraw)
            new clientThread("Клиент 1", "WITHDRAW", this, host).start();
        else if (ae.getSource() == client2Withdraw)
            new clientThread("Клиент 2", "WITHDRAW", this, host).start();
    }

    // Отображение состояния счёта
    public void showMessage(String message) {
        accountField.setText(message.substring(message.indexOf(':', message.indexOf(':') + 1) + 1));
        log.append(message + "\n");
    }

    // IP-адреса этого компьютера в локальной сети (для ввода на других компьютерах)
    static String localAddresses() {
        String result = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback())
                    continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address)
                        result += address.getHostAddress() + " ";
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка " + e);
        }
        return result.trim();
    }

    public static void main(String args[]) {
        if (args.length == 0) { // сервер и клиенты на одном компьютере
            serv f = new serv();
            f.setTitle("Счёт (сервер, IP: " + localAddresses() + ")");
            f.setSize(400, 400);
            f.setVisible(true);
            new Account(f).start();
        } else { // только клиенты, сервер на другом компьютере
            serv f = new serv(args[0]);
            f.setTitle("Счёт (клиент, сервер: " + args[0] + ")");
            f.setSize(400, 400);
            f.setVisible(true);
        }
    }
}
