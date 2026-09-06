package Client.config;

/**
 * Client-side server connection configuration.
 *
 * <p>The default socket port must match the backend {@code SocketServer} default
 * ({@code ${server.socket.port:9090}}). The client speaks the raw newline-delimited
 * socket protocol, NOT HTTP, so it must target the socket port, not the HTTP port.
 *
 * <p>Override at runtime with {@code -Dxclone.server.port=<port>}.
 */
public record ServerConfig(String host, int port, int timeoutMs)
{
    public static final String DEFAULT_HOST = "localhost";

    /** Matches {@code logic_core...SocketServer} {@code ${server.socket.port:9090}}. */
    public static final int DEFAULT_PORT = 9090;

    public static final String PORT_PROPERTY = "xclone.server.port";

    public static ServerConfig defaultLocal()
    {
        int port = Integer.getInteger(PORT_PROPERTY, DEFAULT_PORT);
        return new ServerConfig(DEFAULT_HOST, port, 0);
    }
}
