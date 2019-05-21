package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TestClient {
  public static void main(String[] args) {
    CountDownLatch countDown = new CountDownLatch(1);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername("guest");
    factory.setPassword("guest");
    factory.setHost("127.0.0.1");
    try (Connection con = factory.newConnection(); Channel chn = con.createChannel()) {
      AtomicLong receivedMessages = new AtomicLong();
      String consumerTag =
          chn.basicConsume("product.catalog_item.sync", true, new DefaultConsumer(chn) {
            @Override
            public void handleDelivery(String tag, Envelope envelope, BasicProperties properties,
                byte[] body) throws IOException {
              long actualCount = receivedMessages.incrementAndGet();
              if (actualCount % 1000 == 0) {
                System.out.println("Received " + actualCount + " messages so far.");
              }
              // countDown.countDown();
            }
          });
      System.out.println(consumerTag);
      countDown.await();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
