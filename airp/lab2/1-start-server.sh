#!/bin/sh
# Сервер счёта (Mac или Linux). Адреса этого компьютера показаны в заголовке окна.
cd "$(dirname "$0")"
java -jar serv.jar
