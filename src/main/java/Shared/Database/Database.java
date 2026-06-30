package Shared.Database;

import Shared.Database.DatabaseInitializer.DatabaseCreator;
import Shared.Database.XMLManager.PersistenceXmlReader;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Database
{
    private static final String PERSISTENCE_FOLDER_PATH = "META-INF/";
    private static final String PERSISTENCE_FILE_NAME = "persistence.xml";
    private static final String PERSISTENCE_UNIT_NAME = "X-Clone-PU";

    private final EntityManagerFactory emf;

    public Database()
    {
        String url = PersistenceXmlReader.readFromPersistenceXml(PERSISTENCE_FOLDER_PATH + PERSISTENCE_FILE_NAME,
                PERSISTENCE_UNIT_NAME, "jakarta.persistence.jdbc.url");
        String user = PersistenceXmlReader.readFromPersistenceXml(PERSISTENCE_FOLDER_PATH + PERSISTENCE_FILE_NAME,
                PERSISTENCE_UNIT_NAME, "jakarta.persistence.jdbc.user");
        String password = PersistenceXmlReader.readFromPersistenceXml(PERSISTENCE_FOLDER_PATH + PERSISTENCE_FILE_NAME,
                PERSISTENCE_UNIT_NAME, "jakarta.persistence.jdbc.password");

        String databaseName = url.substring(url.lastIndexOf('/') + 1);
        String defaultDatabaseUrl = url.substring(0, url.lastIndexOf('/')) + "/postgres";

        ConnectionDAO connectionDAO = new ConnectionDAO(defaultDatabaseUrl, user, password);

        DatabaseCreator.createDatabaseIfNotExists(connectionDAO, databaseName);

        this.emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }
}
