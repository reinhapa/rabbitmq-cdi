package net.reini.rabbitmq.cdi;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConsumerContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);

  private final ConnectionConfig config;
  private final ConnectionRepository connectionRepository;
  private final List<ConsumerHolder> consumerHolders;
  private final Condition noConnectionCondition;
  private final DeclarerRepository<QueueDeclaration> declarerRepository;
  private final ReentrantLock lock;

  private ConsumerContainerWatcherThread consumerWatcherThread;
  private ConsumerHolderFactory consumerHolderFactory;

  private volatile boolean connectionAvailable = false;

  ConsumerContainer(ConnectionConfig config, ConnectionRepository connectionRepository,
      DeclarerRepository<QueueDeclaration> declarerRepository) {
    this(config, connectionRepository, declarerRepository, new CopyOnWriteArrayList<>(),
        new ConsumerHolderFactory(), new ReentrantLock());
  }

  ConsumerContainer(ConnectionConfig config, ConnectionRepository connectionRepository,
      DeclarerRepository<QueueDeclaration> declarerRepository, List<ConsumerHolder> consumerHolders,
      ConsumerHolderFactory consumerHolderFactory, ReentrantLock lock) {
    this.config = config;
    this.connectionRepository = connectionRepository;
    this.consumerHolders = consumerHolders;
    this.consumerHolderFactory = consumerHolderFactory;
    this.lock = lock;
    this.noConnectionCondition = lock.newCondition();
    this.declarerRepository = declarerRepository;
  }

  public void addConsumer(EventConsumer<?> consumer, String queue, boolean autoAck,
      int prefetchCount, List<QueueDeclaration> declarations) {
    ConsumerHolder consumerHolder = consumerHolderFactory.createConsumerHolder(consumer, queue,
        autoAck, prefetchCount, connectionRepository, config, declarations, declarerRepository);
    consumerHolders.add(consumerHolder);
  }

  public void start() {
    connectionRepository.registerConnectionListener(config,
        new ContainerConnectionListener(this, lock, noConnectionCondition));
    connectionRepository.connect(config);
    consumerWatcherThread = new ConsumerContainerWatcherThread(this,
        config.getFailedConsumerActivationRetryTime(), lock, noConnectionCondition);
    consumerWatcherThread.start();
  }

  public void stop() {
    consumerWatcherThread.stopThread();
  }

  public void setConnectionAvailable(boolean connectionAvailable) {
    this.connectionAvailable = connectionAvailable;
  }

  boolean ensureConsumersAreActive() {
    boolean allConsumersActive = true;
    for (ConsumerHolder consumerHolder : consumerHolders) {
      try {
        consumerHolder.activate();
      } catch (Exception e) {
        allConsumersActive = false;
        LOGGER.warn("failed to activate consumer", e);
      }
    }
    return allConsumersActive;
  }

  boolean isConnectionAvailable() {
    return connectionAvailable;
  }

  void deactivateAllConsumer() {
    consumerHolders.forEach(consumer -> consumer.deactivate());
  }
}
