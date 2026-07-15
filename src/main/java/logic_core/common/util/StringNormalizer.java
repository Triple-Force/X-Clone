package logic_core.common.util;

public final class StringNormalizer
{
    private StringNormalizer()
    {
    }

    public static String normalize(String value)
    {
        if (value == null)
        {
            return null;
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeLower(String value)
    {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    public static String normalizeUsername(String username)
    {
        if (username == null)
        {
            return null;
        }

        return username.trim().toLowerCase();
    }

    public static String normalizeEmail(String email)
    {
        if (email == null)
        {
            return null;
        }

        return email.trim().toLowerCase();
    }
}
