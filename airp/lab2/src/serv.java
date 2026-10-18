import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Enumeration;

// Вариант 2: два клиента, каждый в своём потоке.
//
// ТЕОРИЯ, которую спрашивает преподаватель (из чата группы и контрольных вопросов):
// - Сокет: конечная точка соединения = IP-адрес + порт. ServerSocket слушает порт и через
//   accept() получает сокет клиента; Socket(host, port) - клиентская сторона.
// - IP - адрес компьютера в сети, порт (0-65535) - номер программы на нём. Здесь порт 3001.
//   IP своего компьютера: ipconfig или метод localAddresses() ниже; 127.0.0.1 - "этот же компьютер".
// - TCP: соединение, гарантия доставки и порядка (Socket/ServerSocket, используем его).
//   UDP: датаграммы без гарантий, быстрее (DatagramSocket). IP - только адресация и маршрут,
//   TCP/UDP работают поверх IP (стек TCP/IP).
// - Данные отправляются через PrintStream(s.getOutputStream()) методом println + flush(),
//   принимаются через getInputStream() и readLine(). Сокет двусторонний: клиент тоже пишет
//   серверу (команда ADD/WITHDRAW) - так выполняется пересылка "от клиента к серверу".
// - Буфер (3 назначения): выравнивание скорости диска и ОП, асинхронный ввод-вывод, кэширование.
//   Поэтому flush() нужен: он проталкивает накопленные в буфере данные в сеть.
// - Асинхронность: каждый клиент в отдельном Thread, окно не зависает на время обмена.
// - Потоки (Thread) работают в одной программе и обмениваются данными через общую
//   (static) переменную amount; процессы - отдельные программы, связь только через сокеты.
// - Стек - локальные переменные и вызовы методов потока; куча - объекты (new Socket, new Thread).
// - PrintStream/PrintWriter - классы текстового вывода (print, println, printf).
// Клиент 1 добавляет и снимает деньги со счёта, клиент 2 только снимает.
//
// Запуск:
//   java -jar serv.jar             - сервер и клиенты на одном компьютере (адрес 127.0.0.1)
//   java -jar serv.jar 192.168.0.5 - только клиенты, сервер работает на компьютере с указанным адресом

// Серверный поток: хранит счёт и обрабатывает запросы клиентов
class Account extends Thread {
    ServerSocket server;
    String amountstring;
    static int amount = 200;  // общий счёт, начальная сумма 200
    serv window; // окно, в журнал которого сервер записывает запросы

    public Account(serv window) {
        this.window = window;
    }

    public void run() {
        try {
            server = new ServerSocket(3001); // сервер слушает порт 3001
        } catch (Exception e) {
            System.out.println("Ошибка соединения+" + e);
        }
        while (true) {  // бесконечный цикл: обслуживаем клиентов по одному
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
                int amountcur = ((int) (Math.random() * 1000));  // случайная сумма 0..999
                if (request.equals("ADD"))  // ADD - добавить, иначе (WITHDRAW) - снять
                    amount += amountcur;
                else
                    amount -= amountcur;
                amountstring = Integer.toString(amount);
                ps.println("Account:" + amountstring); // передача строки клиенту
                ps.flush();
                // Запись в журнал сервера: откуда пришёл запрос и каким стал счёт
                window.log.append("[сервер] " + request + " от " + s.getInetAddress().getHostAddress() + ", счёт: " + amountstring + "\n");
                window.accountField.setText(amountstring);  // обновить поле счёта на сервере
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
            s = new Socket(host, 3001);  // подключение к серверу (адрес + порт)
            dis = new DataInputStream(s.getInputStream());
        } catch (Exception e) {
            System.out.println("Ошибка:  " + e);
        }
    }

    public void run() {  // работа клиента в отдельном потоке
        if (s == null) { // соединение не установлено
            window.log.append(name + ": нет связи с сервером " + host + "\n");
            return;
        }
        try {
            // Отправка запроса серверу
            PrintStream ps = new PrintStream(s.getOutputStream());
            ps.println(request);  // клиент -> сервер: "ADD" или "WITHDRAW"
            ps.flush();

            String msg = dis.readLine(); // Клиент читает строку из сокета
            if (msg != null) {
                System.out.println(name + ": " + msg);
                window.showMessage(name + ": " + msg);  // показать новый счёт в окне
            }
            s.close();
        } catch (Exception e) {
            System.out.println("ERRORR+" + e);
        }
    }
}

// Окно приложения
public class serv extends Frame implements ActionListener {
    // Кнопки клиентов: клиент 1 добавляет и снимает, клиент 2 только снимает
    Button client1Add = new Button("Клиент 1: добавить");
    Button client1Withdraw = new Button("Клиент 1: снять");
    Button client2Withdraw = new Button("Клиент 2: снять");
    // Поле текущего счёта и журнал запросов
    TextField accountField = new TextField("200", 10);
    TextArea log = new TextArea(8, 40);
    String host; // адрес компьютера, на котором работает сервер

    public serv() {
        this("127.0.0.1");  // по умолчанию сервер на этом же компьютере
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

        client1Add.addActionListener(this);  // кнопки -> actionPerformed()
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
            new clientThread("Клиент 1", "ADD", this, host).start();  // новый поток на каждое нажатие
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
        // без параметров - сервер + клиенты; с адресом - только клиенты
        if (args.length == 0) { // сервер и клиенты на одном компьютере
            serv f = new serv();
            f.setTitle("Счёт (сервер, IP: " + localAddresses() + ")");
            f.setSize(400, 400);
            f.setVisible(true);
            new Account(f).start();  // запуск потока-сервера
        } else { // только клиенты, сервер на другом компьютере
            serv f = new serv(args[0]);
            f.setTitle("Счёт (клиент, сервер: " + args[0] + ")");
            f.setSize(400, 400);
            f.setVisible(true);
        }
    }
}
