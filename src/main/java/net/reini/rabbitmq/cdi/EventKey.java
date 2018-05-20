package net.reini.rabbitmq.cdi;

import javax.enterprise.event.TransactionPhase;

final class EventKey<T> {
  private final Class<T> type;
  private final TransactionPhase phase;

  static <T> EventKey<T> of(Class<T> type, TransactionPhase phase) {
    return new EventKey<>(type, phase);
  }
  
  private EventKey(Class<T> type, TransactionPhase phase) {
    this.phase = phase;
    this.type = type;
  }
  
  @Override
  public int hashCode() {
    return type.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof EventKey)) {
      return false;
    }
    EventKey<?> other = (EventKey<?>)obj;
    return phase.equals(other.phase) && type.equals(other.type);
  }
}
