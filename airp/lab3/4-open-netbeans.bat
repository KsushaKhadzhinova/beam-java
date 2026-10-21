@echo off
chcp 65001 > nul
rem Открывает оба проекта лабы в NetBeans (портативная копия на диске E).
set JDK=E:\tools\jdk17
set NB=E:\beam-java\Apache NetBeans\bin\netbeans64.exe
start "" "%NB%" --jdkhome "%JDK%" --open "%~dp0lab_servlet" --open "%~dp0apacheclient"
