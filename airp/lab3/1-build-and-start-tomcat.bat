@echo off
chcp 65001 > nul
rem Пути указаны для ноутбука автора. Если JDK, Maven или Tomcat лежат в других папках, измените три строки ниже.
set JAVA_HOME=E:\tools\jdk17
set CATALINA_HOME=E:\tools\apache-tomcat-9.0.122
set MVN=E:\tools\apache-maven-3.10.0\bin\mvn.cmd
cd /d "%~dp0lab_servlet"
echo Сборка WAR-архива (Maven)...
call "%MVN%" -q package
if errorlevel 1 (
  echo Ошибка сборки
  pause
  exit /b 1
)
copy /y target\lab_servlet.war "%CATALINA_HOME%\webapps\lab_servlet.war"
echo Запуск Tomcat в отдельном окне...
start "Tomcat" "%CATALINA_HOME%\bin\catalina.bat" run
echo Подождите 5-10 секунд и откройте http://localhost:8080/lab_servlet/
pause
