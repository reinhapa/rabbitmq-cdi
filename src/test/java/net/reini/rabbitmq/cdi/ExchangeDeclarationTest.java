package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;

class ExchangeDeclarationTest {

  private static final int EXPECTED_HASHCODE = -606493483;

  @Test
  void testExchangeType() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    ExchangeDeclaration result = sut.withExchangeType(BuiltinExchangeType.FANOUT);
    assertEquals(BuiltinExchangeType.FANOUT.getType(), result.getExchangeType());
  }

  @Test
  void testEquals() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    sut.withDurable(true);
    sut.withAutoDelete(true);
    sut.withExchangeType("FANOUT");
    sut.withArgument("key", Long.valueOf(1));


    assertNotEquals(sut, null);
    assertNotEquals(sut, new Object());
    assertNotEquals(sut, new ExchangeDeclaration("hello2"));
    assertEquals(sut, sut);

    ExchangeDeclaration copy = new ExchangeDeclaration("hello");
    copy.withDurable(true);
    copy.withAutoDelete(true);
    copy.withExchangeType("FANOUT");
    copy.withArgument("key", Long.valueOf(1));

    assertEquals(sut, copy);

    copy.withDurable(false);
    assertNotEquals(sut, copy);
    copy.withDurable(true);
    assertEquals(sut, copy);

    copy.withAutoDelete(false);
    assertNotEquals(sut, copy);
    copy.withAutoDelete(true);
    assertEquals(sut, copy);

    copy.withExchangeType("DIRECT");
    assertNotEquals(sut, copy);
    copy.withExchangeType("FANOUT");
    assertEquals(sut, copy);

    copy.withArgument("test", "test");
    assertNotEquals(sut, copy);

    assertNotEquals(new ExchangeDeclaration("hello"), new ExchangeDeclaration("hello2"));
  }

  @Test
  void testHashCode() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    sut.withDurable(true);
    sut.withAutoDelete(true);
    sut.withExchangeType("FANOUT");
    sut.withArgument("key", Long.valueOf(1));
    int result = sut.hashCode();
    assertEquals(EXPECTED_HASHCODE,result);
  }

  @Test
  void testToString() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    sut.withExchangeType(BuiltinExchangeType.DIRECT);
    sut.withAutoDelete(true);
    sut.withDurable(true);
    sut.withArgument("key","value");
    
    String result = sut.toString();
    System.out.println(result);
    assertEquals("exchange declaration for exchangeName='hello', exchangeType='direct', durable=true, autoDelete=true, arguments={key=value}",result);

  }
}
