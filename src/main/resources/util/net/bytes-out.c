#include <stdio.h>
#include <unistd.h>

#define BUFSIZE 128

int main() {
    setuid(0);
    char *cmd = "iptables -L MCSRV_OUT -nvx | tail -1 | grep -Po '^\\s*[0-9]+\\s+\\K([0-9]+)'";

    char buf[BUFSIZE];
    FILE *fp;

    setuid(0);
    if ((fp = popen(cmd, "r")) == NULL) {
        return -1;
    }

    fgets(buf, BUFSIZE, fp);
    printf(buf);

    if (pclose(fp)) {
        return -1;
    }

    return 0;
}
