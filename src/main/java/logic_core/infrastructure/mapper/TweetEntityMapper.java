package logic_core.infrastructure.mapper;

import logic_core.domain.model.TweetModel;
import logic_core.infrastructure.persistence.entity.tweet.TweetEntity;

public class TweetEntityMapper {

    public static TweetModel toModel(TweetEntity entity) {
        if (entity == null) return null;
        return TweetModel.builder()
                .id(entity.getId())
                .authorId(entity.getAuthor() != null ? entity.getAuthor().getId() : null)
                .content(entity.getContent())
                .repliedToTweetId(entity.getReplyTo() != null ? entity.getReplyTo().getId() : null)
                .retweetedTweetId(entity.getRetweetOf() != null ? entity.getRetweetOf().getId() : null)
                .quotedTweetId(entity.getQuoteOf() != null ? entity.getQuoteOf().getId() : null)
                .pinned(entity.isPinned())
                .deleted(entity.isDeleted())
                .scheduledAt(entity.getScheduledAt())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void updateEntity(TweetEntity entity, TweetModel model) {
        if (entity == null || model == null) return;
        entity.setContent(model.getContent());
        entity.setPinned(model.isPinned());
        entity.setScheduledAt(model.getScheduledAt());
        entity.setPublishedAt(model.getPublishedAt());
        if (model.isDeleted()) entity.markDeleted();
    }
}
