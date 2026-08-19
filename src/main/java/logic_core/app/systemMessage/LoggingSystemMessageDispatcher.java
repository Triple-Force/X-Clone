package logic_core.app.systemMessage;

import java.util.Objects;

public class LoggingSystemMessageDispatcher implements SystemMessageDispatcher
{
    @Override
    public void dispatch(SystemMessageModel message)
    {
        Objects.requireNonNull(message, "message must not be null");

        System.out.println("[SYSTEM MESSAGE]");
        System.out.println("id=" + message.id());
        System.out.println("recipientId=" + message.recipientId());
        System.out.println("type=" + message.type());
        System.out.println("priority=" + message.priority());
        System.out.println("title=" + message.title());
        System.out.println("body=" + message.body());
        System.out.println("createdAt=" + message.createdAt());
        System.out.println("metadata=" + message.metadata());
    }
}
