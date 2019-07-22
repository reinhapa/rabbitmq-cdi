package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class DeclarerRepositoryTest {
  private static final String EXPECTED_QUEUE_NAME = "queue";
  @Mock
  private Channel channelMock;

  @Test
  void testRepository() throws IOException {
    DeclarerRepository sut = new DeclarerRepository();
    List<Declaration> declarations = new ArrayList<>();
    declarations.add(new QueueDeclaration(EXPECTED_QUEUE_NAME));
    sut.declare(channelMock, declarations);
    verify(channelMock).queueDeclare(EXPECTED_QUEUE_NAME, false, false, false, new HashMap<>());
  }
}
