package logic_core.common.security;

import java.util.UUID;

public class TokenGenerator
{
    public String generateToken()
    {
        return UUID.randomUUID().toString();
    }
}
