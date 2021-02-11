/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
  private final DeclarerRepository declarerRepository;
  private final ReentrantLock lock;

  private ConsumerContainerWatcherThread consumerWatcherThread;
  private ConsumerHolderFactory consumerHolderFactory;

  private volatile boolean connectionAvailable = false;

  ConsumerContainer(ConnectionConfig config, ConnectionRepository connectionRepository,
      DeclarerRepository declarerRepository) {
    this(config, connectionRepository, declarerRepository, new CopyOnWriteArrayList<>(),
        new ConsumerHolderFactory(), new ReentrantLock());
  }

  ConsumerContainer(ConnectionConfig config, ConnectionRepository connectionRepository,
      DeclarerRepository declarerRepository, List<ConsumerHolder> consumerHolders,
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
      int prefetchCount, List<Declaration> declarations) {
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
