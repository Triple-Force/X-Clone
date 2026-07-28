package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.HashtagFollow.HashtagFollow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HashtagFollowDao extends GenericDAO<HashtagFollow>
{
    public HashtagFollowDao()
    {
        super(HashtagFollow.class);
    }

    public Optional<HashtagFollow> findRelation(UUID userId, UUID hashtagId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT hf
                        FROM HashtagFollow hf
                        WHERE hf.user.id = :userId
                          AND hf.hashtag.id = :hashtagId
                          AND hf.user.isDeleted = false
                        """,
                        query -> query
                                .setParameter("userId", userId)
                                .setParameter("hashtagId", hashtagId)
                )
        );
    }

    public List<HashtagFollow> findByUserId(UUID userId)
    {
        return findByJpql(
                """
                SELECT hf
                FROM HashtagFollow hf
                WHERE hf.user.id = :userId
                  AND hf.user.isDeleted = false
                """,
                query -> query.setParameter("userId", userId)
        );
    }

    public List<HashtagFollow> findByHashtagId(UUID hashtagId)
    {
        return findByJpql(
                """
                SELECT hf
                FROM HashtagFollow hf
                WHERE hf.hashtag.id = :hashtagId
                  AND hf.user.isDeleted = false
                """,
                query -> query.setParameter("hashtagId", hashtagId)
        );
    }
}
