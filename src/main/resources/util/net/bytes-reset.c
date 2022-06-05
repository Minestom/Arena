#include <stdio.h>
#include <unistd.h>

int main() {
    setuid(0);
    system("iptables -L -Z MCSRV_OUT -v");
    system("iptables -L -Z MCSRV_IN -v");
    return 0;
}
