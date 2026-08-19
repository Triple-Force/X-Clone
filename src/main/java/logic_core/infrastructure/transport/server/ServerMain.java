package logic_core.infrastructure.transport.server;

import Server.ServerDAOManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class ServerMain
{
    private static final int DEFAULT_PORT = 8888;

    private ServerMain()
    {
    }

    public static void main(String[] args)
    {
        int port = resolvePort(args);


        System.out.println("Warmup...");
        ServerDAOManager.getInstance();
        System.out.println("Warmup finished.");

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();

        RequestDispatcher dispatcher = new RequestDispatcher(gson);
        SocketServer server = new SocketServer(port, dispatcher, gson);

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "xclone-server-shutdown"));

        try
        {
            System.out.println("XClone server starting on port " + port + "...");
            server.start();
        }
        catch (Exception e)
        {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static int resolvePort(String[] args)
    {
        if (args != null && args.length > 0)
        {
            String first = args[0].trim();

            if (first.startsWith("--port="))
            {
                return parsePort(first.substring("--port=".length()));
            }

            if ("--port".equals(first) && args.length > 1)
            {
                return parsePort(args[1].trim());
            }

            return parsePort(first);
        }

        String envPort = System.getenv("XCLONE_SERVER_PORT");
        if (envPort != null && !envPort.isBlank())
        {
            return parsePort(envPort.trim());
        }

        return DEFAULT_PORT;
    }

    private static int parsePort(String value)
    {
        try
        {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535)
            {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            return port;
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid port: " + value, e);
        }
    }
}