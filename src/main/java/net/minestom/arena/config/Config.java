package net.minestom.arena.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public record Config(Server server, Proxy proxy, Permissions permissions, Prometheus prometheus) {
    public record Server(@Default("0.0.0.0") String host, @Default("25565") int port, @Default("[\"Line1\",\"Line2\"]") List<String> motd) {
        public SocketAddress address() {
            return new InetSocketAddress(host, port);
        }
    }

    public record Proxy(@Default("false") boolean enabled, @Default("forwarding-secret") String secret) {
        @Override
        public String toString() {
            return "Proxy[enabled="+enabled+", secret=<hidden>]";
        }
    }

    public record Permissions(@Default("[]") List<String> operators) {}

    public record Prometheus(@Default("false") boolean enabled, @Default("9090") int port) {}
}
