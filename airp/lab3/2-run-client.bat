@echo off
chcp 65001 > nul
rem Клиент (отдельный проект). Tomcat должен быть запущен (скрипт 1-build-and-start-tomcat.bat).
set JAVA_HOME=E:\tools\jdk17
set MVN=E:\tools\apache-maven-3.10.0\bin\mvn.cmd
cd /d "%~dp0apacheclient"
if not exist target\apacheclient-1.0.jar call "%MVN%" -q package
"%JAVA_HOME%\bin\java.exe" -Dfile.encoding=UTF-8 -jar target\apacheclient-1.0.jar
pause
