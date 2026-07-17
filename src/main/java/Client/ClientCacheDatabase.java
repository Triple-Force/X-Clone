package Client;

import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;

@Getter
public class ClientCacheDatabase
{
    public /* needed for ClientDAOManager access */ static final String PERSISTENCE_UNIT_NAME = "X-Clone-Client-Cache-PU";

    private final EntityManagerFactory emf;

    public ClientCacheDatabase()
    {
        this.emf = ClientDAOManager.getInstance().getEmf();
    }

    public void close()
    {
        if (emf != null && emf.isOpen())
            emf.close();
    }
}
