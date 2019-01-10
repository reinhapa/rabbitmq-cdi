package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QueueDeclarationTest {

  @Test
  void testToString() {
    QueueDeclaration sut = new QueueDeclaration("hello");
    sut.withExclusiveAccess(true);
    sut.withDurable(true);
    sut.withAutoDelete(true);
    sut.withArgument("key","value");

    String result = sut.toString();
    System.out.println(result);

  }
}