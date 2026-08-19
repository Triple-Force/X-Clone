package logic_core.infrastructure.mapper;

import Shared.Models.BaseEntity;
import Shared.Models.Tweet.Tweet;
import Shared.Models.TweetMention.TweetMention;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
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


    public static Tweet toPersistence(TweetModel model, EntityManager entityManager)
    {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(entityManager, "entityManager must not be null");
        Objects.requireNonNull(model.getAuthorId(), "authorId must not be null");

        User authorRef = entityManager.getReference(User.class, model.getAuthorId());

        Tweet.TweetBuilder builder = Tweet.builder()
                .author(authorRef)
                .content(model.getContent())
                .repliedToTweet(safeTweetRef(entityManager, model.getRepliedToTweetId()))
                .retweetedTweet(safeTweetRef(entityManager, model.getRetweetedTweetId()))
                .quotedTweet(safeTweetRef(entityManager, model.getQuotedTweetId()))
                .isPinned(model.isPinned())
                .scheduledAt(model.getScheduledAt())
                .publishedAt(model.getPublishedAt());

        Tweet entity = builder.build();

        if (model.isDeleted())
        {
            entity.setDeleted(true);
        }

        return entity;
    }


    public static void updateEntity(Tweet entity, TweetModel model, EntityManager entityManager) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(entityManager, "entityManager must not be null");

        if (model.getAuthorId() != null) {
            entity.setAuthor(entityManager.getReference(User.class, model.getAuthorId()));
        }

        entity.setContent(model.getContent());
        entity.setRepliedToTweet(safeTweetRef(entityManager, model.getRepliedToTweetId()));
        entity.setRetweetedTweet(safeTweetRef(entityManager, model.getRetweetedTweetId()));
        entity.setQuotedTweet(safeTweetRef(entityManager, model.getQuotedTweetId()));
        entity.setPinned(model.isPinned());
        entity.setScheduledAt(model.getScheduledAt());
        entity.setPublishedAt(model.getPublishedAt());
        entity.setDeleted(model.isDeleted());
    }

    private static Tweet safeTweetRef(EntityManager em, UUID tweetId)
    {
        if (tweetId == null)
        {
            return null;
        }
        return em.getReference(Tweet.class, tweetId);
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
