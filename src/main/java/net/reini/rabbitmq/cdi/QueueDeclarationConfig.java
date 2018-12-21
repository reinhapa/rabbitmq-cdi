package net.reini.rabbitmq.cdi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class QueueDeclarationConfig {
  private final Set<QueueDeclaration> queueDeclarations;

  QueueDeclarationConfig() {
    this.queueDeclarations = new HashSet<>();
  }

  void addQueueDeclaration(QueueDeclaration queueDeclaration) {
    this.queueDeclarations.add(queueDeclaration);
  }

  List<QueueDeclaration> getQueueDeclarations() {
    return new ArrayList<>(queueDeclarations);
  }
}
