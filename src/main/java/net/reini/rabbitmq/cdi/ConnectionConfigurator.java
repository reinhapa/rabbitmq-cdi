package net.reini.rabbitmq.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.rabbitmq.client.ConnectionFactory;

// @Singleton
@ApplicationScoped
public class ConnectionConfigurator {
  @Inject
  ConnectionFactory connectionFactory;

  public void configureFactory(Class<? extends EventBinder> eventBinderClass) {
    // First, look up if there is a single connection configuration
    ConnectionConfiguration connectionConfiguration =
        eventBinderClass.getAnnotation(ConnectionConfiguration.class);
    if (connectionConfiguration != null) {
      connectionFactory.setHost(connectionConfiguration.host());
      connectionFactory.setVirtualHost(connectionConfiguration.virtualHost());
      connectionFactory.setPort(connectionConfiguration.port());
      connectionFactory.setConnectionTimeout(connectionConfiguration.timeout());
      connectionFactory.setRequestedHeartbeat(connectionConfiguration.heartbeat());
      connectionFactory.setUsername(connectionConfiguration.username());
      connectionFactory.setPassword(connectionConfiguration.password());
      connectionFactory.setRequestedFrameMax(connectionConfiguration.frameMax());
    }
  }
}
