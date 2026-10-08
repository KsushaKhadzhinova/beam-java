@echo off
chcp 65001 > nul
netsh advfirewall firewall delete rule name="AIRP lab2 port 3001"
echo Правило удалено.
pause
