package Shared.Database;

import jakarta.persistence.EntityManager;

public class EntityManagerContext {
    private static final ThreadLocal<EntityManager> context = new ThreadLocal<>();

    /**
     * تنظیم EntityManager برای ترد جاری
     */
    public static void set(EntityManager em) {
        context.set(em);
    }

    /**
     * دریافت EntityManager مربوط به ترد جاری
     * اگر در کانتکست وجود نداشته باشد، Exception پرتاب می‌کند تا از دسترسی‌های غیرمجاز جلوگیری شود.
     */
    public static EntityManager get() {
        EntityManager em = context.get();
        if (em == null) {
            throw new IllegalStateException("EntityManager در کانتکست ترد جاری یافت نشد! مطمئن شوید که تراکنش در RequestDispatcher آغاز شده است.");
        }
        return em;
    }

    /**
     * پاک‌سازی و بستن کانتکست ترد جاری (بسیار حیاتی برای جلوگیری از Memory Leak در Thread Poolها)
     */
    public static void clear() {
        EntityManager em = context.get();
        try {
            if (em != null && em.isOpen()) {
                em.close();
            }
        } finally {
            context.remove();
        }
    }
}
