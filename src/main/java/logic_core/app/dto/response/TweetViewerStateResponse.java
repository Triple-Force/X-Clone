package logic_core.app.dto.response;

public record TweetViewerStateResponse(
        boolean liked,
        boolean retweeted,
        boolean bookmarked,
        boolean canEdit,
        boolean canDelete,
        boolean canPin
) {}
