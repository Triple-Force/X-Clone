package logic_core.infrastructure.mapper;

import Shared.Models.BaseEntity;
import Shared.Models.Tweet.Tweet;
import Shared.Models.TweetMention.TweetMention;
import logic_core.domain.model.TweetModel;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TweetPersistenceMapper
{

    public static TweetModel toModel(Tweet entity)
    {
        if (entity == null) return null;

        return TweetModel.builder()
                .id(entity.getId())
                .authorId(entity.getAuthor() != null ? entity.getAuthor().getId() : null)
                .content(entity.getContent())
                .repliedToTweetId(entity.getRepliedToTweet() != null ? entity.getRepliedToTweet().getId() : null)
                .retweetedTweetId(entity.getRetweetedTweet() != null ? entity.getRetweetedTweet().getId() : null)
                .quotedTweetId(entity.getQuotedTweet() != null ? entity.getQuotedTweet().getId() : null)
                .pinned(entity.isPinned())
                .deleted(entity.isDeleted())
                .scheduledAt(entity.getScheduledAt())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isEdited(entity.getEditHistory() != null && !entity.getEditHistory().isEmpty())
                .mentionedUserIds(mapMentionEntitiesToUserIds(entity.getMentions()))
                .build();
    }


    private static List<UUID> mapMentionEntitiesToUserIds(List<TweetMention> mentions)
    {
        if (mentions == null || mentions.isEmpty())
        {
            return List.of();
        }

        return mentions.stream()
                .filter(Objects::nonNull)
                .map(TweetMention::getMentionedUser)
                .filter(Objects::nonNull)
                .map(BaseEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
