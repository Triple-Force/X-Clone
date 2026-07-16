package logic_core.common.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextParser
{
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private TextParser()
    {
    }

    public static Set<String> extractHashtags(String text)
    {
        Set<String> hashtags = new HashSet<>();

        if (text == null)
        {
            return hashtags;
        }

        Matcher matcher = HASHTAG_PATTERN.matcher(text);

        while (matcher.find())
        {
            hashtags.add(matcher.group(1).toLowerCase());
        }

        return hashtags;
    }

    public static Set<String> extractMentions(String text)
    {
        Set<String> mentions = new HashSet<>();

        if (text == null)
        {
            return mentions;
        }

        Matcher matcher = MENTION_PATTERN.matcher(text);

        while (matcher.find())
        {
            mentions.add(matcher.group(1).toLowerCase());
        }

        return mentions;
    }

    public static String trimToLength(String text, int maxLength)
    {
        if (text == null)
        {
            return null;
        }

        if (text.length() <= maxLength)
        {
            return text;
        }

        return text.substring(0, maxLength);
    }
}
