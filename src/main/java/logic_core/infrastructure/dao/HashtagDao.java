package logic_core.infrastructure.dao;

import Shared.Models.Hashtag.Hashtag;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class HashtagDao extends AbstractJpaDao<Hashtag>
{
    public HashtagDao(EntityManager entityManager)
    {
        super(entityManager, Hashtag.class);
    }

    public Optional<Hashtag> findByName(String tag)
    {
        Objects.requireNonNull(tag, "tag must not be null");

        List<Hashtag> hashtags = entityManager.createQuery(
                        "SELECT h FROM Hashtag h WHERE h.tag = :tag",
                        Hashtag.class
                )
                .setParameter("tag", tag.toLowerCase(Locale.ROOT))
                .setMaxResults(1)
                .getResultList();

        return hashtags.stream().findFirst();
    }


    public List<Hashtag> findTrendingHashtags(int limit)
    {
        return entityManager.createQuery(
                        "SELECT h FROM Hashtag h ORDER BY h.usageCount DESC",
                        Hashtag.class
                )
                .setMaxResults(limit)
                .getResultList();
    }
}
