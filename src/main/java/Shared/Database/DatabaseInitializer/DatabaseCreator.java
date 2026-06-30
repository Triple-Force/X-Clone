package Shared.Database.DatabaseInitializer;

import Shared.Database.ConnectionDAO;

import java.sql.*;

public class DatabaseCreator
{
    public static void createDatabaseIfNotExists(ConnectionDAO connectionData, String databaseName)
    {
        try (Connection connection = DriverManager.getConnection(connectionData.getUrl(), connectionData.getUser(),
                connectionData.getPassword()); Statement statement = connection.createStatement())
        {
            if (!doesDatabaseExists(statement, databaseName))
                createDatabase(statement, databaseName, connectionData.getUser());
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static boolean doesDatabaseExists(Statement statement, String databaseName) throws SQLException
    {
        String checkDbSql = "SELECT 1 FROM pg_database WHERE datname = ?";
        try (PreparedStatement preparedStatement = statement.getConnection().prepareStatement(checkDbSql))
        {
            preparedStatement.setString(1, databaseName);
            return preparedStatement.executeQuery().next();
        }
    }

    private static void createDatabase(Statement statement, String databaseName, String appUser) throws SQLException
    {
        String createDatabaseQuery = String.format(
                "CREATE DATABASE %s WITH OWNER = %s ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' " + "LC_CTYPE = 'en_US.UTF-8' TEMPLATE = template0",
                databaseName, appUser);

        statement.executeUpdate(createDatabaseQuery);
    }
}
