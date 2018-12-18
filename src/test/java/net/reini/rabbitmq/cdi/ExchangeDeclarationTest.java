package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.rabbitmq.client.BuiltinExchangeType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExchangeDeclarationTest {

  @Test
  void testExchangeType() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    ExchangeDeclaration result = sut.withExchangeType(BuiltinExchangeType.FANOUT);
    assertEquals(BuiltinExchangeType.FANOUT.getType(),result.getExchangeType());
  }

  @Test
  void testEquals()
  {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    sut.withDurable(true);
    sut.withAutoDelete(true);
    sut.withExchangeType("FANOUT");
    Map<String, Object> arguments=new HashMap<>();
    sut.withArguments(arguments);


    assertNotEquals(sut, null);
    assertNotEquals(sut, new Object());
    assertNotEquals(sut, new ExchangeDeclaration("hello2"));
    assertEquals(sut,sut);

    ExchangeDeclaration copy = new ExchangeDeclaration("hello");
    copy.withDurable(true);
    copy.withAutoDelete(true);
    copy.withExchangeType("FANOUT");
    Map<String, Object> arguments2=new HashMap<>();
    copy.withArguments(arguments2);

    assertEquals(sut,copy);

    copy.withDurable(false);
    assertNotEquals(sut,copy);
    copy.withDurable(true);
    assertEquals(sut,copy);

    copy.withAutoDelete(false);
    assertNotEquals(sut,copy);
    copy.withAutoDelete(true);
    assertEquals(sut,copy);

    copy.withExchangeType("DIRECT");
    assertNotEquals(sut,copy);
    copy.withExchangeType("FANOUT");
    assertEquals(sut,copy);

    arguments2.put("test","test");
    copy.withArguments(arguments2);
    assertNotEquals(sut,copy);

    assertNotEquals(new ExchangeDeclaration("hello"),new ExchangeDeclaration("hello2"));
  }
}