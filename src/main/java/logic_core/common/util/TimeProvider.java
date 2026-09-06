package logic_core.common.util;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class TimeProvider
{
    public  OffsetDateTime now()
    {
        return OffsetDateTime.now();
    }
}
