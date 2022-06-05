package net.minestom.arena.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public record Config(Server server, Proxy proxy, Permissions permissions, Prometheus prometheus) {
    public Config {
        if (server == null) server = new Server();
        if (proxy == null) proxy = new Proxy();
        if (permissions == null) permissions = new Permissions();
    }

    public Config() {
        this(null, null, null, null);
    }

    public record Server(String host, int port) {
        public Server {
            if (host == null) host = "0.0.0.0";
            if (port == 0) port = 25565;
        }

        public Server() {
            this(null, 0);
        }

        public SocketAddress address() {
            return new InetSocketAddress(host, port);
        }
    }

    public record Proxy(String secret) {
        public Proxy {
            if (secret == null) secret = "none";
        }

        public Proxy() {
            this(null);
        }

        public boolean enabled() {
            return secret() != null
                    && !secret().equals("none")
                    && !secret().equals("");
        }

        @Override
        public String toString() {
            return "Proxy[secret=<hidden>]";
        }
    }

    public record Permissions(List<String> operators) {
        public Permissions {
            if (operators == null) operators = List.of();
        }

        public Permissions() {
            this(null);
        }
    }

    public record Prometheus(int port) {
    }
}
