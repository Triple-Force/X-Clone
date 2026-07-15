package logic_core.infrastructure.dao;

import jakarta.persistence.EntityManager;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractJpaDao<T>
{
    protected final EntityManager entityManager;
    private final Class<T> entityClass;

    protected AbstractJpaDao(EntityManager entityManager, Class<T> entityClass)
    {
        this.entityManager = Objects.requireNonNull(
                entityManager,
                "entityManager must not be null"
        );

        this.entityClass = Objects.requireNonNull(
                entityClass,
                "entityClass must not be null"
        );
    }

    protected void persist(T entity)
    {
        entityManager.persist(entity);
    }

    protected T merge(T entity)
    {
        return entityManager.merge(entity);
    }

    protected void remove(T entity)
    {
        T managedEntity = entityManager.contains(entity)
                ? entity
                : entityManager.merge(entity);

        entityManager.remove(managedEntity);
    }

    protected Optional<T> findById(Object id)
    {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }
}
