package Shared.Database.DAO;

import Shared.Database.EntityManagerContext;
import Shared.Models.SoftDeletable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class GenericDAO<T>
{
    Class<T> type;

    protected EntityManager getEntityManager()
    {
        return EntityManagerContext.get();
    }

    private <R> R executeRead(Function<EntityManager, R> action)
    {
        return action.apply(getEntityManager());

    }

    private void executeWriteVoid(Consumer<EntityManager> action)
    {
        action.accept(getEntityManager());
    }

    public void insert(T obj)
    {
        executeWriteVoid(em ->
        {
            em.persist(obj);
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
        String jpql = SoftDeletable.class.isAssignableFrom(
                type) ? "SELECT e FROM " + type.getSimpleName() + " e WHERE e.isDeleted = false" : "SELECT e FROM " + type.getSimpleName() + " e";

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
        String jpql = SoftDeletable.class.isAssignableFrom(
                type) ? "SELECT COUNT(e) FROM " + type.getSimpleName() + " e WHERE e.isDeleted = false" : "SELECT COUNT(e) FROM " + type.getSimpleName() + " e";

        return countByJpql(jpql, null);
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

    private Object getId(T obj)
    {
        return getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(obj);
    }

    public T update(T obj)
    {
        Object id = getId(obj);
        if (id == null)
            throw new EntityNotFoundException(
                    "Cannot update " + type.getSimpleName() + ": no existing row for id " + id);

        return executeRead(em ->
        {
            T existing = em.find(type, id);
            if (existing == null)
            {
                throw new EntityNotFoundException(
                        "Cannot update " + type.getSimpleName() + ": no existing row for id " + id
                );
            }
            return em.merge(obj);
        });
    }

    public T upsert(T obj)
    {
        return executeRead(em -> em.merge(obj));
    }

    public void upsertAll(List<T> objects)
    {
        executeWriteVoid(em -> objects.forEach(em::merge));
    }

    public void delete(T obj)
    {
        if (obj instanceof SoftDeletable softDeletable)
        {
            executeWriteVoid(em ->
            {
                softDeletable.setDeleted(true);
                softDeletable.redact();
                softDeletable.onSoftDelete(em);
                em.merge(obj);
            });
        }
        else
        {
            hardDelete(obj);
        }
    }

    public void deleteById(Object id)
    {
        executeWriteVoid(em ->
        {
            T entity = em.find(type, id);
            if (entity != null)
            {
                if (entity instanceof SoftDeletable softDeletable)
                {
                    softDeletable.setDeleted(true);
                    softDeletable.redact();
                    softDeletable.onSoftDelete(em);
                    em.merge(entity);
                }
                else
                {
                    em.remove(entity);
                }
            }
        });
    }

    public void hardDelete(T obj)
    {
        executeWriteVoid(em ->
        {
            Object managed = em.contains(obj) ? obj : em.merge(obj);
            em.remove(managed);
        });
    }

    public void hardDeleteById(Object id)
    {
        executeWriteVoid(em ->
        {
            T entity = em.find(type, id);
            if (entity != null)
            {
                em.remove(entity);
            }
        });
    }

    public <R> List<R> findProjectionByJpql(
            String jpql,
            Class<R> resultType,
            Consumer<TypedQuery<R>> queryConsumer)
    {
        return executeRead(em ->
        {
            TypedQuery<R> query =
                    em.createQuery(jpql, resultType);

            if (queryConsumer != null)
            {
                queryConsumer.accept(query);
            }

            return query.getResultList();
        });
    }

    public <R> List<R> findProjectionByJpql(
            String jpql,
            Class<R> resultType,
            Consumer<TypedQuery<R>> queryConsumer,
            int limit,
            int offset)
    {
        return executeRead(em ->
        {
            TypedQuery<R> query =
                    em.createQuery(jpql, resultType);

            if (queryConsumer != null)
            {
                queryConsumer.accept(query);
            }

            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.getResultList();
        });
    }
}