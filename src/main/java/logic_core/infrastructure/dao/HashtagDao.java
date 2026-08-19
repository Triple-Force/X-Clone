package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Hashtag.Hashtag;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HashtagDao extends GenericDAO<Hashtag>
{
    public HashtagDao()
    {
        super(Hashtag.class);
    }

    public Optional<Hashtag> findByName(String tag)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT h FROM Hashtag h WHERE h.tag = :tag",
                        query -> query.setParameter("tag", tag.toLowerCase(Locale.ROOT))
                )
        );
    }

    public List<Hashtag> findTrendingHashtags(int limit)
    {
        List<Hashtag> hashtags = findByJpql(
                "SELECT h FROM Hashtag h ORDER BY h.usageCount DESC"
        );

        return hashtags.stream().limit(limit).toList();
    }
}
