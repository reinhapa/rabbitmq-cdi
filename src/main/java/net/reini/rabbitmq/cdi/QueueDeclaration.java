package net.reini.rabbitmq.cdi;

import java.util.HashMap;
import java.util.Map;

public class QueueDeclaration
{
    private String queueName;
    private boolean durable = false;
    private boolean autoDelete = true;
    private Map<String, Object> arguments = new HashMap<>();
    private boolean exclusive;

    public QueueDeclaration(String queueName)
    {
        this.queueName = queueName;
    }

    public QueueDeclaration withDurable(boolean durable)
    {
        this.durable = durable;
        return this;
    }

    public QueueDeclaration withAutoDelete(boolean autoDelete)
    {
        this.autoDelete = autoDelete;
        return this;
    }

    public QueueDeclaration withArguments(Map<String, Object> arguments)
    {
        this.arguments = arguments;
        return this;
    }

    public QueueDeclaration withExclusiveAccess(boolean exclusive)
    {
        this.exclusive = exclusive;
        return this;
    }


    boolean isExclusive()
    {
        return exclusive;
    }

    String getQueueName()
    {
        return this.queueName;
    }


    boolean isDurable()
    {
        return durable;
    }

    boolean isAutoDelete()
    {
        return autoDelete;
    }

    Map<String, Object> getArguments()
    {
        return arguments;
    }

    @Override
    public String toString()
    {
        return "queueName='" + queueName + '\'' +
                ", durable=" + durable +
                ", autoDelete=" + autoDelete +
                ", arguments=" + arguments +
                ", exclusive=" + exclusive;
    }
}
