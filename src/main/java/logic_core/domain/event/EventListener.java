package logic_core.domain.event;

public interface EventListener<E extends DomainEvent>
{
    void onEvent(E event);
}
