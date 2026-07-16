package logic_core.common.util;

import java.util.UUID;

public final class IdUtil
{

    private IdUtil()
    {
    }

    public static UUID newUUID()
    {
        return UUID.randomUUID();
    }

    public static String newIdString()
    {
        return UUID.randomUUID().toString();
    }
}
