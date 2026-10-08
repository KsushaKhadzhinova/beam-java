@echo off
chcp 65001 > nul
cd /d "%~dp0"
if "%1"=="" (
  set /p SERVER_IP=Введите IP компьютера, где запущен сервер, например 192.168.0.5: 
) else (
  set SERVER_IP=%1
)
java -jar serv.jar %SERVER_IP%
if errorlevel 1 echo Не удалось запустить Java. Установите JDK 11 или новее.
pause
