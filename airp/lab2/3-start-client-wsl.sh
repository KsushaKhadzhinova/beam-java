#!/bin/sh
# Клиент в Linux (WSL) на том же ноутбуке, где сервер в Windows.
# Адрес Windows берётся автоматически (это шлюз виртуальной сети WSL).
cd "$(dirname "$0")"
HOST=$(ip route | awk '/default/ {print $3}')
echo "Сервер (Windows): $HOST"
java -jar serv.jar "$HOST"
