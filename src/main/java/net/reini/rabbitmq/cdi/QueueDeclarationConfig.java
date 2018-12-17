package net.reini.rabbitmq.cdi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueueDeclarationConfig
{
    private Set<QueueDeclaration> queueDeclarations;

    public QueueDeclarationConfig()
    {
        this.queueDeclarations = new HashSet<>();
    }

    public void addQueueDeclaration(QueueDeclaration queueDeclaration)
    {
        this.queueDeclarations.add(queueDeclaration);
    }

    List<QueueDeclaration> getQueueDeclarations()
    {
        return new ArrayList<>(queueDeclarations);
    }
}
