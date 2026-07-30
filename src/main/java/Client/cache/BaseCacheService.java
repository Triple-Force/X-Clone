package Client.cache;

import Shared.Database.DAO.GenericDAO;

import java.util.List;

public abstract class BaseCacheService<T>
{
    protected final GenericDAO<T> dao;

    protected BaseCacheService(GenericDAO<T> dao)
    {
        this.dao = dao;
    }

    public void save(T entity)
    {
        dao.upsert(entity);
    }

    public void saveAll(List<T> entities)
    {
        dao.upsertAll(entities);
    }

    public T findById(Object id)
    {
        return dao.findById(id);
    }

    public List<T> findAll()
    {
        return dao.findAll();
    }

    public boolean exists(Object id)
    {
        return dao.existsById(id);
    }

    public void delete(Object id)
    {
        dao.deleteById(id);
    }

    public long count()
    {
        return dao.count();
    }
}