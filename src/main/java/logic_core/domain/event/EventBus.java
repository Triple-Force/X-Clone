package logic_core.domain.event;

public interface EventBus extends EventPublisher
{
    <E extends DomainEvent> void register(Class<E> eventType, EventListener<E> listener);
    void shutdown();
}
