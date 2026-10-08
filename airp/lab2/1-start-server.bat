@echo off
chcp 65001 > nul
cd /d "%~dp0"
echo Запуск сервера счёта. IP-адреса этого компьютера показаны в заголовке окна.
echo Закройте окно программы, чтобы остановить сервер.
java -jar serv.jar
if errorlevel 1 echo Не удалось запустить Java. Установите JDK 11 или новее.
pause
