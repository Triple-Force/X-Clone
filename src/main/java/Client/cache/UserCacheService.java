package Client.cache;

import Client.ClientDAOManager;
import Shared.Models.User.User;
import logic_core.app.dto.response.*;

import java.util.List;
import java.util.UUID;

public class UserCacheService extends BaseCacheService<User>
{
    public UserCacheService()
    {
        super(ClientDAOManager.getInstance().getUserDAO());
    }

    // =========================
    // Read
    // =========================

    public ProfileInfoResponse getProfile(UUID userId)
    {
        return dao.findProjectionByJpql(
                """
                SELECT new logic_core.app.dto.response.ProfileInfoResponse(
                    u.id,
                    u.username,
                    u.displayName,
                    u.bio,
                    u.avatarUrl,
                    u.bannerUrl,

                    (SELECT COUNT(f)
                     FROM Follow f
                     WHERE f.following.id = u.id),

                    (SELECT COUNT(f)
                     FROM Follow f
                     WHERE f.follower.id = u.id),

                    (SELECT COUNT(t)
                     FROM Tweet t
                     WHERE t.author.id = u.id
                       AND t.isDeleted = false),

                    u.isVerified,
                    u.createdAt
                )
                FROM User u
                WHERE u.id = :userId
                  AND u.isDeleted = false
                """,
                ProfileInfoResponse.class,
                q -> q.setParameter("userId", userId)
        ).stream().findFirst().orElse(null);
    }

    // =========================
    // Write
    // =========================

    public void cacheProfile(ProfileInfoResponse response)
    {
        User user = dao.findOneByJpql(
                """
                SELECT u
                FROM User u
                WHERE u.id = :id
                """,
                q -> q.setParameter("id", response.userId())
        );

        if (user == null)
        {
            return;
        }

        user.setUsername(response.username());
        user.setDisplayName(response.displayName());
        user.setBio(response.bio());
        user.setAvatarUrl(response.avatarUrl());
        user.setBannerUrl(response.banner());

        dao.update(user);
    }

    // =========================
    // Update
    // =========================

    public void updateProfile(
            UUID userId,
            String username,
            String displayName)
    {
        User user = dao.findOneByJpql(
                """
                SELECT u
                FROM User u
                WHERE u.id = :id
                """,
                q -> q.setParameter("id", userId)
        );

        if (user == null)
        {
            return;
        }

        user.setUsername(username);
        user.setDisplayName(displayName);

        dao.update(user);
    }

    public void updateBio(UpdateBioResponse response)
    {
        User user = dao.findOneByJpql(
                """
                SELECT u
                FROM User u
                WHERE u.id = :id
                """,
                q -> q.setParameter("id", response.userId())
        );

        if (user == null)
        {
            return;
        }

        user.setBio(response.bio());

        dao.update(user);
    }

    public void updateAvatar(UpdateAvatarResponse response)
    {
        User user = dao.findOneByJpql(
                """
                SELECT u
                FROM User u
                WHERE u.id = :id
                """,
                q -> q.setParameter("id", response.userId())
        );

        if (user == null)
        {
            return;
        }

        user.setAvatarUrl(response.avatarUrl());

        dao.update(user);
    }

    public void updateBanner(UpdateBannerResponse response)
    {
        User user = dao.findOneByJpql(
                """
                SELECT u
                FROM User u
                WHERE u.id = :id
                """,
                q -> q.setParameter("id", response.userId())
        );

        if (user == null)
        {
            return;
        }

        user.setBannerUrl(response.bannerUrl());

        dao.update(user);
    }

    public void updateCompleteProfile(
            UpdateCompleteProfileResponse response)
    {
        if (response == null || response.profile() == null)
        {
            return;
        }

        ProfileInfoResponse profile = response.profile();

        User user = dao.findOneByJpql(
                """
                SELECT u
                FROM User u
                WHERE u.id = :id
                """,
                q -> q.setParameter("id", profile.userId())
        );

        if (user == null)
        {
            return;
        }

        user.setUsername(profile.username());
        user.setDisplayName(profile.displayName());
        user.setBio(profile.bio());
        user.setAvatarUrl(profile.avatarUrl());
        user.setBannerUrl(profile.banner());

        dao.update(user);
    }

    // =========================
    // Delete
    // =========================

    public void deleteUser(UUID userId)
    {
        dao.deleteById(userId);
    }

    public void clear()
    {
        dao.findAll().forEach(dao::delete);
    }
}