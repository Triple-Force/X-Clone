package logic_core.domain.event;

public interface EventPublisher
{
    void publish(DomainEvent event);
}
