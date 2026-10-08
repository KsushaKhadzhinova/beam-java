#!/bin/sh
# Клиент (Mac или Linux): sh 2-start-client.sh 192.168.0.5
cd "$(dirname "$0")"
SERVER_IP="$1"
if [ -z "$SERVER_IP" ]; then
  printf "Введите IP компьютера, где запущен сервер, например 192.168.0.5: "
  read SERVER_IP
fi
java -jar serv.jar "$SERVER_IP"
