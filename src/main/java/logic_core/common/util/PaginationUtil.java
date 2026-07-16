package logic_core.common.util;

public final class PaginationUtil
{
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private PaginationUtil()
    {
    }

    public static int normalizePage(Integer page)
    {
        if (page == null || page < 0)
        {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int normalizeSize(Integer size)
    {
        if (size == null || size <= 0)
        {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    public static int calculateOffset(int page, int size)
    {
        return page * size;
    }
}
