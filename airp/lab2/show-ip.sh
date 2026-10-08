#!/bin/sh
# IP-адреса этого компьютера (Mac или Linux)
if command -v hostname >/dev/null 2>&1 && hostname -I >/dev/null 2>&1; then
  hostname -I
else
  ifconfig | grep "inet " | grep -v 127.0.0.1
fi
