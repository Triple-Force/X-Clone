package Client.config;

public record ServerConfig(String host, int port, int timeoutMs)
{
    public static ServerConfig defaultLocal()
    {
        return new ServerConfig("127.0.0.1", 8080, 0);
    }
}
