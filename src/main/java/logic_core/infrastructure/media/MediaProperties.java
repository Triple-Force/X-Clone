package logic_core.infrastructure.media;


public class MediaProperties
{
    private final String rootPath;

    public MediaProperties(String rootPath)
    {
        this.rootPath = rootPath;
    }


    public String getRootPath()
    {
        return rootPath;
    }
}