# АИРП – лабораторные работы

Лабораторные работы по дисциплине «Администрирование и программирование распределенных приложений» (Java). Вариант 2. Каждая работа находится в папке `airp/labN`, отчёт в папке `report`.

| Папка | Лабораторная работа |
|---|---|
| `airp/lab1` | №1. Работа с Web-ресурсами (поиск названия лекарства по симптомам) |
| `airp/lab2` | №2. Работа на основе сокетных соединений (два клиента-потока) |
| `airp/lab3` | №3. Работа с серверными классами (сервлеты, словарь, сервлет с рисунком) |

## Запуск

Нужен JDK 17 или новее.

**Лабораторная 1** (из папки `airp/lab1`):

```bash
javac -encoding UTF-8 -d out src/web_res_lab/Web_res_lab.java
java -Dfile.encoding=UTF-8 -cp out web_res_lab.Web_res_lab
```

**Лабораторная 2** (из папки `airp/lab2`):

```bash
javac -encoding UTF-8 -d out src/serv.java
java -Dfile.encoding=UTF-8 -cp out serv
```

**Лабораторная 3** (нужны Maven и Apache Tomcat 9):

```bash
cd airp/lab3/lab_servlet
mvn package
# скопировать target/lab_servlet.war в каталог webapps Tomcat 9 и запустить сервер,
# приложение откроется по адресу http://localhost:8080/lab_servlet/
cd ../apacheclient
mvn package
# запуск клиента: класс apacheclient.ApacheClient (зависимости подключены через Maven)
```
