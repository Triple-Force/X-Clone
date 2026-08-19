package logic_core.infrastructure.mapper;

import Shared.Models.Notification.Notification;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.NotificationModel;

public class NotificationPersistenceMapper
{
    public static NotificationModel toModel(Notification entity)
    {
        if (entity == null)
        {
            return null;
        }

        return NotificationModel.builder()
                .id(entity.getId())
                .recipientId(entity.getRecipient() != null ? entity.getRecipient().getId() : null)
                .actorId(entity.getActor() != null ? entity.getActor().getId() : null)
                .tweetId(entity.getTweet() != null ? entity.getTweet().getId() : null)
                .type(entity.getType())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }


    public static Notification toPersistence(NotificationModel model, EntityManager entityManager)
    {
        if (model == null)
        {
            return null;
        }

        User recipient = entityManager.getReference(User.class, model.getRecipientId());
        User actor = entityManager.getReference(User.class, model.getActorId());
        Tweet tweet = entityManager.getReference(Tweet.class, model.getTweetId());

        return Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(model.getType())
                .tweet(tweet)
                .isRead(model.isRead())
                .build();
    }
}

