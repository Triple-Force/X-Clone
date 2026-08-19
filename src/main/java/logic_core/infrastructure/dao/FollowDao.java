package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Follow.Follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FollowDao extends GenericDAO<Follow>
{
    public FollowDao()
    {
        super(Follow.class);
    }

    public void insert(Follow follow)
    {
        super.insert(follow);
    }

    public void delete(Follow follow)
    {
        super.hardDelete(follow);
    }

    public Optional<Follow> findRelation(UUID followerId, UUID followingId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT f
                        FROM Follow f
                        WHERE f.follower.id = :followerId
                          AND f.following.id = :followingId
                          AND f.follower.isDeleted = false
                          AND f.following.isDeleted = false
                        """,
                        query -> query
                                .setParameter("followerId", followerId)
                                .setParameter("followingId", followingId)
                )
        );
    }

    public List<Follow> findByFollowerId(UUID followerId)
    {
        return findByJpql(
                """
                SELECT f
                FROM Follow f
                WHERE f.follower.id = :followerId
                  AND f.follower.isDeleted = false
                  AND f.following.isDeleted = false
                """,
                query -> query.setParameter("followerId", followerId)
        );
    }

    public List<Follow> findByFollowingId(UUID followingId)
    {
        return findByJpql(
                """
                SELECT f
                FROM Follow f
                WHERE f.following.id = :followingId
                  AND f.follower.isDeleted = false
                  AND f.following.isDeleted = false
                """,
                query -> query.setParameter("followingId", followingId)
        );
    }

    public long countFollowers(UUID userId)
    {
        return countByJpql(
                """
                SELECT COUNT(f)
                FROM Follow f
                WHERE f.following.id = :userId
                  AND f.follower.isDeleted = false
                  AND f.following.isDeleted = false
                """,
                query -> query.setParameter("userId", userId)
        );
    }

    public long countFollowing(UUID userId)
    {
        return countByJpql(
                """
                SELECT COUNT(f)
                FROM Follow f
                WHERE f.follower.id = :userId
                  AND f.follower.isDeleted = false
                  AND f.following.isDeleted = false
                """,
                query -> query.setParameter("userId", userId)
        );
    }
}
