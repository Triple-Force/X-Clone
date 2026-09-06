package logic_core.infrastructure.repository;

import logic_core.domain.model.FollowRelation;
import logic_core.infrastructure.mapper.FollowEntityMapper;
import logic_core.infrastructure.persistence.entity.follow.FollowEntity;
import logic_core.infrastructure.persistence.entity.follow.FollowEntityId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure-level adapter for Follow persistence operations.
 * <p>
 * Uses {@link FollowJpaRepository} and {@link UserJpaRepository} for
 * Spring Data JPA operations, and {@link FollowEntityMapper} for
 * entity/domain mapping.
 * <p>
 * This adapter will be used by {@code JpaRelationshipRepository} to
 * replace the legacy {@code FollowDao} dependency.
 */
@Component
@Transactional
public class FollowRepositoryAdapter {

    private final FollowJpaRepository followJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public FollowRepositoryAdapter(FollowJpaRepository followJpaRepository,
                                   UserJpaRepository userJpaRepository) {
        this.followJpaRepository = followJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * Persists a new follow relation.
     *
     * @param follow the domain follow relation to persist
     */
    public void saveFollow(FollowRelation follow) {
        var follower = userJpaRepository.getReferenceById(follow.getFollowerId());
        var following = userJpaRepository.getReferenceById(follow.getFollowingId());
        FollowEntity entity = FollowEntityMapper.toPersistence(follow, follower, following);
        followJpaRepository.save(entity);
    }

    /**
     * Hard-deletes an existing follow relation.
     *
     * @param follow the domain follow relation to delete
     */
    public void deleteFollow(FollowRelation follow) {
        FollowEntityId id = new FollowEntityId(
                follow.getFollowerId(),
                follow.getFollowingId()
        );
        followJpaRepository.deleteById(id);
    }

    /**
     * Finds a specific follow relation between two users.
     * Filters out soft-deleted users.
     *
     * @param followerId  the follower's user ID
     * @param followingId the following user's ID
     * @return the follow relation if it exists and both users are active
     */
    public Optional<FollowRelation> findFollowRelation(UUID followerId, UUID followingId) {
        return followJpaRepository.findRelation(followerId, followingId)
                .map(FollowEntityMapper::toDomain);
    }

    /**
     * Finds all users that followerId is following.
     * Filters out soft-deleted users.
     *
     * @param followerId the follower's user ID
     * @return list of follow relations
     */
    public List<FollowRelation> findByFollowerId(UUID followerId) {
        return followJpaRepository.findByFollowerId(followerId)
                .stream()
                .map(FollowEntityMapper::toDomain)
                .toList();
    }

    /**
     * Finds all followers of a user.
     * Filters out soft-deleted users.
     *
     * @param followingId the user whose followers to find
     * @return list of follow relations
     */
    public List<FollowRelation> findByFollowingId(UUID followingId) {
        return followJpaRepository.findByFollowingId(followingId)
                .stream()
                .map(FollowEntityMapper::toDomain)
                .toList();
    }

    /**
     * Checks if followerId is following followingId.
     * Equivalent to findFollowRelation().isPresent().
     *
     * @param followerId  the potential follower
     * @param followingId the potential following
     * @return true if the follow relation exists
     */
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followJpaRepository.findRelation(followerId, followingId).isPresent();
    }

    /**
     * Checks if a follow relation exists in either direction.
     * Equivalent to isFollowing(A, B) || isFollowing(B, A).
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return true if either user follows the other
     */
    public boolean existsFollowRelation(UUID userId1, UUID userId2) {
        return isFollowing(userId1, userId2) || isFollowing(userId2, userId1);
    }

    /**
     * Counts how many users follow the given user.
     * Filters out soft-deleted users.
     *
     * @param userId the user whose followers to count
     * @return the follower count
     */
    public long countFollowers(UUID userId) {
        return followJpaRepository.countFollowers(userId);
    }

    /**
     * Counts how many users the given user follows.
     * Filters out soft-deleted users.
     *
     * @param userId the user whose followings to count
     * @return the following count
     */
    public long countFollowing(UUID userId) {
        return followJpaRepository.countFollowing(userId);
    }
}
