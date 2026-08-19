package logic_core.common.result;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T>
{
    private final List<T> items;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageResult(List<T> items, int page, int size, long totalElements)
    {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }

}
