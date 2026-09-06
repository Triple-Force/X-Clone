package logic_core.infrastructure.transport.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "logic_core")
public class ServerMain
{
    public static void main(String[] args)
    {
        SpringApplication.run(ServerMain.class, args);
    }
}