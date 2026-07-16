package logic_core.infrastructure.dao;

import Shared.Models.Follow.Follow;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FollowDao extends AbstractJpaDao<Follow>
{
    public FollowDao(EntityManager entityManager)
    {
        super(entityManager, Follow.class);
    }

    public void insert(Follow follow)
    {
        Objects.requireNonNull(follow, "follow entity must not be null");
        persist(follow);
    }

    public void delete(Follow follow)
    {
        Objects.requireNonNull(follow, "follow entity must not be null");
        remove(follow);
    }

    public Optional<Follow> findRelation(UUID followerId, UUID followingId)
    {
        Objects.requireNonNull(followerId, "followerId must not be null");
        Objects.requireNonNull(followingId, "followingId must not be null");

        List<Follow> results = entityManager.createQuery(
                        "SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId",
                        Follow.class
                )
                .setParameter("followerId", followerId)
                .setParameter("followingId", followingId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }

    public List<Follow> findByFollowerId(UUID followerId)
    {
        Objects.requireNonNull(followerId, "followerId must not be null");

        return entityManager.createQuery(
                        "SELECT f FROM Follow f WHERE f.follower.id = :followerId",
                        Follow.class
                )
                .setParameter("followerId", followerId)
                .getResultList();
    }

    public List<Follow> findByFollowingId(UUID followingId)
    {
        Objects.requireNonNull(followingId, "followingId must not be null");

        return entityManager.createQuery(
                        "SELECT f FROM Follow f WHERE f.following.id = :followingId",
                        Follow.class
                )
                .setParameter("followingId", followingId)
                .getResultList();
    }

    public long countFollowers(UUID userId)
    {
        Objects.requireNonNull(userId, "userId must not be null");

        Long count = entityManager.createQuery(
                        "SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId",
                        Long.class
                )
                .setParameter("userId", userId)
                .getSingleResult();

        return count != null ? count : 0L;
    }

    public long countFollowing(UUID userId)
    {
        Objects.requireNonNull(userId, "userId must not be null");

        Long count = entityManager.createQuery(
                        "SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId",
                        Long.class
                )
                .setParameter("userId", userId)
                .getSingleResult();

        return count != null ? count : 0L;
    }
}
