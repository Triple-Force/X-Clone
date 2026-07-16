package logic_core.app.dto.request;

/**
 * @param hashtagText Hashtag text without '#'
 */
public record SearchHashtagRequest(
        String hashtagText,
        int page,
        int size
) {}
