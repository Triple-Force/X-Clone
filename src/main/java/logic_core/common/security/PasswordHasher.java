package logic_core.common.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher
{
    private static final int SALT_ROUNDS = 12;

    public String hash(String rawPassword)
    {
        if (rawPassword == null || rawPassword.isBlank())
        {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(SALT_ROUNDS));
    }

    public boolean verify(String rawPassword, String hashedPassword)
    {
        if (rawPassword == null || hashedPassword == null)
        {
            return false;
        }

        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
