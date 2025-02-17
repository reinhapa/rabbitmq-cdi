/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2025 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.reini.rabbitmq.cdi;

import java.util.StringJoiner;

import jakarta.enterprise.event.TransactionPhase;

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
  public String toString() {
    return new StringJoiner(", ", "EventKey[", "]").add(type.getSimpleName()).add(phase.name())
        .toString();
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
    EventKey<?> other = (EventKey<?>) obj;
    return phase.equals(other.phase) && type.equals(other.type);
  }
}
