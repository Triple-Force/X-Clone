package Server;

import Shared.Database.Database;
import Shared.Models.User.User;

public class Server
{
    static void main()
    {
        Database database = new Database();

        // demonstration! must be removed for final production.
        DAOManager daoManager = new DAOManager(database.getEmf());

        User newUser = User.builder().username("Desert").passwordHash("HASHED").bio("bio is bs").email(
                "Desert@gmail.com").displayName("D3s3rt").avatarUrl("avatar_url").bannerUrl("banner_url").build();

        daoManager.getUserDAO().insert(newUser);
        System.out.println("Added user.");

        System.out.println(
                "User Id with name 'Desert': " + daoManager.getUserDAO().findByField("username", "Desert").getId());
        // end of demonstration.
    }
}
