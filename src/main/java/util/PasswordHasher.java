package util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher
{

    public static String hashPassword(String plainTextPassword)
    {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainTextPassword, String hashedWithSalt)
    {
        return BCrypt.checkpw(plainTextPassword, hashedWithSalt);
    }
}
