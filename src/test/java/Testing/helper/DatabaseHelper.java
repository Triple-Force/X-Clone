package Testing.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.UUID;

public class DatabaseHelper
{
    private final EntityManagerFactory emf;

    public DatabaseHelper(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    public void clearDatabase()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();

            // Child tables first
            em.createQuery("DELETE FROM Session").executeUpdate();
            em.createQuery("DELETE FROM Like").executeUpdate();
            em.createQuery("DELETE FROM Follow").executeUpdate();
            em.createQuery("DELETE FROM Block").executeUpdate();
            em.createQuery("DELETE FROM Mute").executeUpdate();

            em.createQuery("DELETE FROM DirectMessage").executeUpdate();
            em.createQuery("DELETE FROM ConversationMember").executeUpdate();
            em.createQuery("DELETE FROM Conversation").executeUpdate();

            em.createQuery("DELETE FROM Tweet").executeUpdate();
            em.createQuery("DELETE FROM Media").executeUpdate();

            em.createQuery("DELETE FROM User").executeUpdate();

            em.getTransaction().commit();
        }
        catch (RuntimeException ex)
        {
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }

            throw ex;
        }
        finally
        {
            em.close();
        }
    }

    public long countUsers()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery(
                    "select count(u) from User u",
                    Long.class
            ).getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

    public long countSessions()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery(
                    "select count(s) from Session s",
                    Long.class
            ).getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

    public EntityManager newEntityManager()
    {
        return emf.createEntityManager();
    }

    // =========================================================
    // Users
    // =========================================================

    public boolean userExists(String username)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            Long count = em.createQuery("""
                    SELECT COUNT(u)
                    FROM User u
                    WHERE u.username = :username
                    """, Long.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return count > 0;
        }
        finally
        {
            em.close();
        }
    }

    // =========================================================
    // Sessions
    // =========================================================

    public boolean sessionExists(UUID sessionId)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            Long count = em.createQuery("""
                    SELECT COUNT(s)
                    FROM Session s
                    WHERE s.id = :id
                    """, Long.class)
                    .setParameter("id", sessionId)
                    .getSingleResult();

            return count > 0;
        }
        finally
        {
            em.close();
        }
    }

    // =========================================================
    // Tweets
    // =========================================================

    public long countTweets()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("""
                    SELECT COUNT(t)
                    FROM Tweet t
                    """, Long.class)
                    .getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

    // =========================================================
    // Likes
    // =========================================================

    public long countLikes()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("""
                    SELECT COUNT(l)
                    FROM Like l
                    """, Long.class)
                    .getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

    // =========================================================
// Follow
// =========================================================

    public long countFollows()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("""
                SELECT COUNT(f)
                FROM Follow f
                """, Long.class)
                    .getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

// =========================================================
// Conversation
// =========================================================

    public long countConversations()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("""
                SELECT COUNT(c)
                FROM Conversation c
                """, Long.class)
                    .getSingleResult();
        }
        finally
        {
            em.close();
        }
    }

// =========================================================
// Message
// =========================================================

    public long countMessages()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("""
                SELECT COUNT(m)
                FROM Message m
                """, Long.class)
                    .getSingleResult();
        }
        finally
        {
            em.close();
        }
    }
}