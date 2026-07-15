package logic_core.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserModel
{
    private UUID id;

    private String username;
    private String email;
    private String passwordHash;

    private String displayName;
    private String bio;
    private String avatarUrl;
    private String bannerUrl;

    private boolean verified;
    private boolean active;
    private boolean deleted;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;


    public static UserModel createNew(UUID id,String username, String email, String passwordHash, OffsetDateTime now)
    {

        System.out.println("+++++++++++in userModel");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Password hash is required");
        if (now == null) throw new IllegalArgumentException("Now is required");

        return UserModel.builder()
                .id(id)
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .displayName(username)
                .bio(null)
                .avatarUrl(null)
                .bannerUrl(null)
                .verified(false)
                .active(true)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }


    public void updateProfile(String displayName, String bio, String avatarUrl, String bannerUrl)
    {
        if (displayName == null || displayName.isBlank())
        {
            throw new IllegalArgumentException("Display name cannot be empty");
        }
        if (bio != null && bio.length() > 160)
        {
            throw new IllegalArgumentException("Bio cannot exceed 160 characters");
        }

        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.bannerUrl = bannerUrl;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updatePasswordHash(String newPasswordHash)
    {
        if (newPasswordHash == null || newPasswordHash.isBlank())
        {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        this.passwordHash = newPasswordHash;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateEmail(String newEmail)
    {
        if (newEmail == null || newEmail.isBlank())
        {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        this.email = newEmail;
        this.updatedAt = OffsetDateTime.now();
    }

    public void verify()
    {
        this.verified = true;
        this.updatedAt = OffsetDateTime.now();
    }

    public void revokeVerification()
    {
        this.verified = false;
        this.updatedAt = OffsetDateTime.now();
    }

    public void deactivate()
    {
        this.active = false;
        this.updatedAt = OffsetDateTime.now();
    }

    public void activate()
    {
        if (this.deleted)
        {
            throw new IllegalStateException("Cannot activate a deleted user account");
        }
        this.active = true;
        this.updatedAt = OffsetDateTime.now();
    }

    public void softDelete()
    {
        this.deleted = true;
        this.active = false;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isAvailable()
    {
        return active && !deleted;
    }

    public boolean hasBio()
    {
        return bio != null && !bio.isBlank();
    }
}
