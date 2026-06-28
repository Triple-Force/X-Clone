package util;

import model.TimelineItem;
import model.Tweet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimelinePrinter
{
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void print(List<TimelineItem> items) {
        System.out.println("========== TIMELINE ==========");

        for (TimelineItem item : items)
        {
            switch (item.getType())
            {
                case NORMAL_TWEET:
                    System.out.println("[TWEET]");
                    System.out.println("Author ID: " + item.getActorUserId());
                    System.out.println("Time: " + format(item.getCreatedAt()));
                    System.out.println("Content: " + item.getTweet().getContent());
                    System.out.println("Likes: " + item.getLikeCount());
                    System.out.println("Liked By You: " + item.isLikedByCurrentUser());
                    System.out.println("Retweeted By You: " + item.isRetweetedByCurrentUser());
                    break;

                case SIMPLE_RETWEET:
                    System.out.println("[RETWEET]");
                    System.out.println("Retweeted by: " + item.getActorUserId());
                    System.out.println("Author: " + item.getTweet().getAuthorId());
                    System.out.println("Content: " + item.getTweet().getContent());
                    System.out.println("Likes: " + item.getLikeCount());
                    System.out.println("Liked By You: " + item.isLikedByCurrentUser());
                    System.out.println("Retweeted By You: " + item.isRetweetedByCurrentUser());
                    break;

                case QUOTE_TWEET:
                    System.out.println("[QUOTE TWEET]");
                    System.out.println("Author ID: " + item.getActorUserId());
                    System.out.println("Time: " + format(item.getCreatedAt()));
                    System.out.println("Content: " + item.getTweet().getContent());
                    System.out.println("Likes: " + item.getLikeCount());
                    System.out.println("Liked By You: " + item.isLikedByCurrentUser());
                    System.out.println("Retweeted By You: " + item.isRetweetedByCurrentUser());

                    Tweet original = item.getOriginalTweet();
                    if (original != null)
                    {
                        System.out.println("Quoted Tweet:");
                        System.out.println("  Author: " + original.getAuthorId());
                        System.out.println("  Content: " + original.getContent());
                    }
                    break;
            }
            System.out.println("----------------------------------------");
        }
    }

    private void printItem(TimelineItem item)
    {
        switch (item.getType())
        {
            case NORMAL_TWEET -> printNormal(item);

            case QUOTE_TWEET -> printQuote(item);

            case SIMPLE_RETWEET -> printRetweet(item);
        }
    }

    private void printNormal(TimelineItem item)
    {
        Tweet tweet = item.getTweet();

        System.out.println("[TWEET]");
        System.out.println("Author ID: " + item.getActorUserId());
        System.out.println("Time: " + format(item.getCreatedAt()));
        System.out.println("Content: " + tweet.getContent());
        System.out.println("Likes: " + item.getLikeCount());
        System.out.println("Liked By You: " + item.isLikedByCurrentUser());
        System.out.println("Retweeted By You: " + item.isRetweetedByCurrentUser());
    }

    private void printQuote(TimelineItem item)
    {
        System.out.println("[QUOTE TWEET]");

        printNormal(item);

        System.out.println("Quoted Tweet:");

        Tweet original = item.getOriginalTweet();

        if (original != null) {
            System.out.println("Author: " + original.getAuthorId());
            System.out.println("Content: " + original.getContent());
        }
    }

    private void printRetweet(TimelineItem item)
    {
        System.out.println("[RETWEET]");
        System.out.println("Retweeted by: " + item.getActorUserId());

        Tweet original = item.getTweet();

        if (original != null)
        {
            System.out.println("Author: " + original.getAuthorId());
            System.out.println("Content: " + original.getContent());
        }
    }

    private String format(LocalDateTime time)
    {
        if (time == null) return "N/A";
        return time.format(FORMATTER);
    }
}
