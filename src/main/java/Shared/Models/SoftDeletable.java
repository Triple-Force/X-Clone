package Shared.Models;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;

public interface SoftDeletable
{
    boolean isDeleted();

    void setDeleted(boolean deleted);

    default void redact()
    {
    }

    default void onSoftDelete(EntityManager em)
    {
    }

    default <E> void hardDeleteWhere(EntityManager em, Class<E> entityClass, String fieldName, Object value)
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<E> delete = cb.createCriteriaDelete(entityClass);
        Root<E> root = delete.from(entityClass);
        delete.where(cb.equal(root.get(fieldName), value));
        em.createQuery(delete).executeUpdate();
    }
}

