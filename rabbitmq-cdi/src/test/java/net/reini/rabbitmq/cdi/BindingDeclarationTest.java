package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BindingDeclarationTest {
  @Test
  void testToString() {
    ExchangeDeclaration exchange = new ExchangeDeclaration("hello");
    QueueDeclaration queue = new QueueDeclaration("hello2");
    BindingDeclaration sut = new BindingDeclaration(queue, exchange);
    sut.withRoutingKey("route");
    sut.withArgument("key", Long.valueOf(1));
    String result = sut.toString();
    assertEquals("queue to exchange binding for queue hello2 to exchange hello, routingKey='route', arguments={key=1}",result);
  }
}