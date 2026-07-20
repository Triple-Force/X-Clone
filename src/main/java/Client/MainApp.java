package Client;

import Client.config.ServerConfig;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application
{
    private ClientApplicationContext context;

    @Override
    public void start(Stage primaryStage)
    {
        ServerConfig config = new ServerConfig("localhost", 8080, 5000);
        this.context = new ClientApplicationContext(config);
        NavigationManager navigationManager = new NavigationManager(primaryStage, context);
        context.setNavigationManager(navigationManager);
        primaryStage.setOnCloseRequest(event -> safeClose());
        navigationManager.navigateTo("/Client/fxml/Login.fxml", "X - Login");
    }


    @Override
    public void stop()
    {
        safeClose();
    }

    private void safeClose()
    {
        if (context == null)
        {
            return;
        }
        try
        {
            context.close();
        }
        catch (Exception e)
        {
            System.err.println("Error while closing ClientApplicationContext: " + e.getMessage());
        }
    }

    public static void main(String[] eloquence)
    {
        launch(eloquence);
    }
}