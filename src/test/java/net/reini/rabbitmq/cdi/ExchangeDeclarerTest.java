package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class ExchangeDeclarerTest {
  private static final String EXPECTED_EXCHANGE_NAME = "exchange";
  private static final String EXPECTED_EXCHANGE_TYPE = "direct";
  private static final String EXPECTED_ARGUMENT_NAME = "argument";
  private static final Object EXPECTED_ARGUMENT_VALUE = new Object();
  private static final boolean EXPECTED_EXCHANGE_AUTODELETE_SETTING = true;
  private static final boolean EXPECTED_EXCHANGE_DURABLE_SETTING = true;

  @Mock
  private Channel channelMock;

  @Test
  void testExchangeDeclaration() throws IOException {

    ExchangeDeclaration exchangeDeclaration = new ExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    exchangeDeclaration.withType(EXPECTED_EXCHANGE_TYPE);
    exchangeDeclaration.withArgument(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);
    exchangeDeclaration.withAutoDelete(EXPECTED_EXCHANGE_AUTODELETE_SETTING);
    exchangeDeclaration.withDurable(EXPECTED_EXCHANGE_DURABLE_SETTING);
    ExchangeDeclarer sut = new ExchangeDeclarer();
    sut.declare(channelMock,exchangeDeclaration);

    verify(channelMock, Mockito.times(1)).exchangeDeclare(exchangeDeclaration.getExchangeName(),
        exchangeDeclaration.getExchangeType(), exchangeDeclaration.isDurable(),
        exchangeDeclaration.isAutoDelete(), exchangeDeclaration.getArguments());
  }

}