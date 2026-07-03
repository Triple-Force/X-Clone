package Server;

import Shared.Database.Database;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class Server
{
    static void main()
    {
        Database database = new Database();

        // demonstration! must be removed for final production.
        DAOManager daoManager = new DAOManager(database.getEmf());
        var userDao = daoManager.getUserDAO();
        var tweetDao = daoManager.getTweetDAO();

        User newUser = User.builder().username("Desert").passwordHash("HASHED").bio("bio is bs").email(
                "Desert@gmail.com").displayName("D3s3rt").avatarUrl("avatar_url").bannerUrl("banner_url").build();

        userDao.insert(newUser);
        System.out.println("Added user.");

        System.out.println("User Id with name 'Desert': " + userDao.findByField("username", "Desert").getId());

        Tweet tweet1 = Tweet.builder().author(newUser).content("First").build();
        Tweet tweet2 = Tweet.builder().author(newUser).content("Second").build();
        Tweet tweet3 = Tweet.builder().author(newUser).content("Third").build();
        tweetDao.insert(tweet1);
        tweetDao.insert(tweet2);
        tweetDao.insert(tweet3);

        User selectedUser = userDao.findById(newUser.getId());

        List<Tweet> tweetsByUser = selectedUser.getTweets();
        for (Tweet tweet : tweetsByUser)
            System.out.println(
                    "TWEET | User: " + tweet.getAuthor().getUsername() + " | Content: " + tweet.getContent() + " | Published At: " + tweet.getPublishedAt().format(
                            DateTimeFormatter.ISO_OFFSET_DATE));

        userDao.delete(selectedUser);
        // end of demonstration.
    }
}
