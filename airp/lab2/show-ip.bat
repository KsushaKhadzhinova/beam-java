@echo off
chcp 65001 > nul
echo IP-адреса этого компьютера:
ipconfig | findstr /i "IPv4"
pause
