# Network Usage Monitoring Utils
This directory contains three executables to provide network usage metrics.
You can replace these binaries with your own solution, for that see the implementation details.

## Using the bundled utils
### Prerequisites
You must have root privileges, iptables and grep installed!
### Setup
If you don't care about the commands you run as root, then for your convenience we provide
you with a `setup.sh`, all you have to do is run it as **root**, and you are ready to go,
otherwise follow the next steps, so you know what you are doing.

**1. Step**
Create chains
```shell
iptables -N MCSRV_OUT
iptables -A OUTPUT -j MCSRV_OUT
iptables -N MCSRV_IN
iptables -A INPUT -j MCSRV_IN
```

**2. Step**
Add rules. Replace `<minecraft-server-port>` with the actual port!
```shell
iptables -A MCSRV_OUT -p tcp --sport <minecraft-server-port>
iptables -A MCSRV_IN -p tcp --dport <minecraft-server-port>
```

**3. Step**
Change file owners, set setuid bit
```shell
chown root: bytes-out
chown root: bytes-in
chown root: bytes-reset
chmod u+s bytes-out
chmod u+s bytes-in
chmod u+s bytes-reset
```

**4. Step**
Create a file named `enabled` to enable network metrics.

## Implementation details
If you choose to replace the bundled utils make sure they return **byte count** without
any prefix or suffix.
### `bytes-out` and `bytes-in`
These are compiled from their respective c source files, internally they use the following
command to retrieve the total bytes:
```shell
iptables -L <chain-name> -nvx | tail -1 | grep -Po '^\s*[0-9]+\s+\K([0-9]+)'
```
it was necessary to wrap this in a binary executable so the server process doesn't
have to be run as root.

### `bytes-reset`
This is used to reset the counters, internally it executes the following commands:
```shell
iptables -L -Z MCSRV_OUT -v
iptables -L -Z MCSRV_IN -v
```
It's wrapped in binary executable for the same reason as above
