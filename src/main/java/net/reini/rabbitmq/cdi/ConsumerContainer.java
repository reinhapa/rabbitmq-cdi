package net.reini.rabbitmq.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class ConsumerContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);

  private final ConnectionConfiguration config;
  private final ConnectionRepository connectionRepository;
  private final List<ConsumerHolder> consumerHolders;

  private final ExchangeDeclarationConfig exchangeDeclarationConfig;
  private final QueueDeclarationConfig queueDeclarationConfig;
  private Thread consumerWatcherThread;
  private volatile boolean connectionAvailable = false;
  private ConsumerHolderFactory consumerHolderFactory;

  ConsumerContainer(ConnectionConfiguration config, ConnectionRepository connectionRepository)
  {
    this(config, connectionRepository, new CopyOnWriteArrayList<>(), new ExchangeDeclarationConfig(), new QueueDeclarationConfig(), new ConsumerHolderFactory());
  }

  ConsumerContainer(ConnectionConfiguration config, ConnectionRepository connectionRepository, List<ConsumerHolder> consumerHolders, ExchangeDeclarationConfig exchangeDeclarationConfig, QueueDeclarationConfig queueDeclarationConfig, ConsumerHolderFactory consumerHolderFactory)
  {
    this.config = config;
    this.connectionRepository = connectionRepository;
    this.consumerHolders = consumerHolders;
    this.exchangeDeclarationConfig = exchangeDeclarationConfig;
    this.queueDeclarationConfig = queueDeclarationConfig;
    this.consumerHolderFactory = consumerHolderFactory;
  }

  public void addConsumer(EventConsumer consumer, String queue, boolean autoAck)
  {
    ConsumerHolder consumerHolder = consumerHolderFactory.createConsumerHolder(consumer, queue, autoAck, connectionRepository, config, exchangeDeclarationConfig, queueDeclarationConfig);
    consumerHolders.add(consumerHolder);
  }

  public void start()
  {
    connectionRepository.registerConnectionListener(config, new ContainerConnectionListener(this));
    connectionRepository.connect(config);
    consumerWatcherThread = new ConsumerContainerWatcherThread(this, config.getFailedConsumerActivationRetryTime());
    consumerWatcherThread.start();
  }

  public void addExchangeDeclaration(ExchangeDeclaration exchangeDeclaration)
  {
    this.exchangeDeclarationConfig.addExchangeDeclaration(exchangeDeclaration);
  }

  public void addQueueDeclaration(QueueDeclaration queueDeclaration)
  {
    this.queueDeclarationConfig.addQueueDeclaration(queueDeclaration);
  }

  boolean ensureConsumersAreActive()
  {
    boolean allConsumersActive = true;
    for (ConsumerHolder consumerHolder : consumerHolders)
    {
      try
      {
        consumerHolder.activate();
      }
      catch (Exception e)
      {
        allConsumersActive = false;
        LOGGER.warn("failed to activate consumer", e);
      }
    }
    return allConsumersActive;

  }

  boolean isConnectionAvailable()
  {
    return connectionAvailable;
  }

  void deactivateAllConsumer()
  {
    consumerHolders.forEach(consumer -> consumer.deactivate());
  }

  public void setConnectionAvailable(boolean connectionAvailable)
  {
    this.connectionAvailable = connectionAvailable;
  }
}
