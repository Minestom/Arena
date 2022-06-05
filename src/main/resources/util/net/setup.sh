#!/bin/sh

# Checks
if [ $# != 1 ]; then
  echo "Skipping iptables due to missing server port number (or too many args), only setting file permissions."
fi

if [ "$(id -u)" != "0" ]; then
  echo "This script must be run as root!"
  exit 1
fi

if ! command -v iptables >/dev/null 2>&1
then
    echo "iptables could not be found"
    exit 1
fi

if ! command -v grep >/dev/null 2>&1
then
    echo "grep could not be found"
    exit 1
fi

if [ "$(echo '    9569 147232704            tcp  --  *      *       0.0.0.0/0            0.0.0.0/0            tcp spt:25565' | grep -Po '^\s*[0-9]+\s+\K([0-9]+)')" != "147232704" ]; then
  echo "grep couldn't filter output"
  exit 1
fi

# Setup
if [ $# = 1 ]; then
  iptables -N MCSRV_OUT
  iptables -A OUTPUT -j MCSRV_OUT
  iptables -N MCSRV_IN
  iptables -A INPUT -j MCSRV_IN
  iptables -A MCSRV_OUT -p tcp --sport "$1"
  iptables -A MCSRV_IN -p tcp --dport "$1"
fi
chown root: bytes-out
chown root: bytes-in
chown root: bytes-reset
chmod u+s bytes-out
chmod u+s bytes-in
chmod u+s bytes-reset
touch enabled

echo "Setup done, bye!"
