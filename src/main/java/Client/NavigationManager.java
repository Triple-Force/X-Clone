package Client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Objects;

public final class NavigationManager
{
    private final Stage stage;
    private final ClientApplicationContext context;

    public NavigationManager(Stage stage, ClientApplicationContext context)
    {
        this.stage = Objects.requireNonNull(stage, "stage must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    public void navigateTo(String fxmlPath, String title)
    {
        Objects.requireNonNull(fxmlPath, "fxmlPath must not be null");

        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null)
            {
                throw new IllegalArgumentException("FXML not found on classpath: " + fxmlPath);
            }

            loader.setControllerFactory(this::createController);

            Parent root = loader.load();
            Scene scene = stage.getScene();
            if (scene == null)
            {
                stage.setScene(new Scene(root));
            }
            else
            {
                scene.setRoot(root);
            }

            if (title != null && !title.isBlank())
            {
                stage.setTitle(title);
            }

            if (!stage.isShowing())
            {
                stage.show();
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to load FXML: " + fxmlPath, e);
        }
    }


    private Object createController(Class<?> type)
    {
        try
        {
            Constructor<?> withContext = type.getConstructor(ClientApplicationContext.class);
            return withContext.newInstance(context);
        }
        catch (NoSuchMethodException ignored)
        {
            // fallback
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException(
                    "Failed to create controller with context: " + type.getName(), e);
        }

        try
        {
            Constructor<?> noArg = type.getDeclaredConstructor();
            noArg.setAccessible(true);
            return noArg.newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException(
                    "Controller must have either "
                            + type.getSimpleName() + "(ClientApplicationContext) "
                            + "or a no-arg constructor: " + type.getName(),
                    e);
        }
    }

    public Stage stage()
    {
        return stage;
    }

    public ClientApplicationContext context()
    {
        return context;
    }
}
