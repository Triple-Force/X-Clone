package logic_core.infrastructure.event;

import logic_core.domain.event.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncEventBus implements EventBus
{
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executor;

    public AsyncEventBus()
    {
        this(Runtime.getRuntime().availableProcessors());
    }

    public AsyncEventBus(int poolSize)
    {
        this.executor = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public <E extends DomainEvent> void register(Class<E> eventType, EventListener<E> listener)
    {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void publish(DomainEvent event)
    {
        System.out.println("event published. in AsyncEventBus");
        List<EventListener<?>> registered = listeners.get(event.getClass());

        if (registered == null || registered.isEmpty())
        {
            return;
        }

        System.out.println(1);

        for (EventListener<?> rawListener : registered)
        {
            @SuppressWarnings("unchecked")
            EventListener<DomainEvent> listener = (EventListener<DomainEvent>) rawListener;

            executor.submit(() -> {
                try
                {
                    listener.onEvent(event);
                }
                catch (Exception ex)
                {

                    System.err.println("Event listener failed for "
                            + event.getClass().getSimpleName() + ": " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void shutdown()
    {
        executor.shutdown();
    }
}
