@echo off
chcp 65001 > nul
echo Открывается входящий порт TCP 3001 в брандмауэре Windows (нужны права администратора).
netsh advfirewall firewall add rule name="AIRP lab2 port 3001" dir=in action=allow protocol=TCP localport=3001
echo Готово. После защиты запустите ADMIN-close-port-3001.bat, чтобы удалить правило.
pause
