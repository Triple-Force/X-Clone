package Shared.Database.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class GenericDAO<T>
{
    private final Class<T> type;
    private final EntityManagerFactory emf;

    private <R> R executeRead(Function<EntityManager, R> action)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return action.apply(em);
        }
    }

    private <R> R executeWrite(Function<EntityManager, R> action)
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            R result = action.apply(em);
            em.getTransaction().commit();
            return result;
        }
        catch (RuntimeException e)
        {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    private void executeWriteVoid(Consumer<EntityManager> action)
    {
        executeWrite(em ->
        {
            action.accept(em);
            return null;
        });
    }

    public T insert(T obj)
    {
        return executeWrite(em ->
        {
            em.persist(obj);
            return obj;
        });
    }

    public void insertAll(List<T> objects)
    {
        executeWriteVoid(em -> objects.forEach(em::persist));
    }

    public T findById(Object id)
    {
        return executeRead(em -> em.find(type, id));
    }

    public List<T> findAll()
    {
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e";
        return executeRead(em -> em.createQuery(jpql, type).getResultList());
    }

    public List<T> findByJpql(String jpql, Consumer<TypedQuery<T>> queryConsumer)
    {
        return executeRead(em ->
        {
            TypedQuery<T> query = em.createQuery(jpql, type);
            if (queryConsumer != null)
                queryConsumer.accept(query);

            return query.getResultList();
        });
    }

    public List<T> findByJpql(String jpql)
    {
        return findByJpql(jpql, null);
    }

    public T findOneByJpql(String jpql, Consumer<TypedQuery<T>> queryConsumer)
    {
        return executeRead(em ->
        {
            TypedQuery<T> query = em.createQuery(jpql, type);
            if (queryConsumer != null)
                queryConsumer.accept(query);

            query.setMaxResults(1);
            List<T> results = query.getResultList();
            return results.isEmpty() ? null : results.getFirst();
        });
    }

    public T findOneByJpql(String jpql)
    {
        return findOneByJpql(jpql, null);
    }

    public long countByJpql(String jpql, Consumer<TypedQuery<Long>> queryConsumer)
    {
        return executeRead(em ->
        {
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            if (queryConsumer != null)
                queryConsumer.accept(query);

            try
            {
                return query.getSingleResult();
            }
            catch (NoResultException e)
            {
                return 0L;
            }
        });
    }

    public long count()
    {
        return countByJpql("SELECT COUNT(e) FROM " + type.getSimpleName() + " e", null);
    }

    public boolean existsById(Object id)
    {
        return findById(id) != null;
    }

    public T findByField(String fieldName, Object value)
    {
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e." + fieldName + " = :value";
        return executeRead(em ->
        {
            TypedQuery<T> query = em.createQuery(jpql, type);
            query.setParameter("value", value);
            List<T> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.getFirst();
        });
    }

    public List<T> findAllByField(String fieldName, Object value)
    {
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e." + fieldName + " = :value";
        return executeRead(em ->
        {
            TypedQuery<T> query = em.createQuery(jpql, type);
            query.setParameter("value", value);
            return query.getResultList();
        });
    }

    public T update(T obj)
    {
        return executeWrite(em -> em.merge(obj));
    }

    public T commit(T obj)
    {
        return update(obj);
    }

    public void delete(T obj)
    {
        executeWriteVoid(em ->
        {
            Object managed = em.contains(obj) ? obj : em.merge(obj);
            em.remove(managed);
        });
    }

    public void deleteById(Object id)
    {
        T entity = findById(id);
        if (entity != null)
            delete(entity);
    }
}