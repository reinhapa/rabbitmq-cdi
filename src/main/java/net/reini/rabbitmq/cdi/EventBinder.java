/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2026 Patrick Reinhart
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

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * <p>
 * Binds incoming CDI events to queues and outgoing CDI events to exchanges of a broker.
 * </p>
 *
 * <p>
 * Inherit from this class and override its {@link #bindEvents()} method to create bindings.
 * </p>
 *
 * <p>
 * <b>Queue example:</b>
 * </p>
 *
 * <pre>
 * protected void bindEvents() {
 *   bind(MyEventOne.class).toQueue(&quot;myQueueOne&quot;);
 *   bind(MyEventTwo.class).toQueue(&quot;myQueueTwo&quot;).autoAck();
 * }
 * </pre>
 *
 * <p>
 * <b>Exchange example:</b>
 * </p>
 *
 * <pre>
 * protected void bindEvents() {
 *   bind(MyEvent.class).toExchange(&quot;myExchange&quot;).withRoutingKey(&quot;myRoutingKey&quot;)
 *       .withPublisherTransactions();
 *   bind(MyEvent.class).toExchange(&quot;myExchange&quot;).withRoutingKey(&quot;myRoutingKey&quot;)
 *       .withEncoder(new MyCustomEncoder()).withPublisherTransactions();
 * }
 * </pre>
 *
 * <p>
 * To initialize the event bindings, inject the instance of this class and call {@link #initialize}.
 * In a web application, you would normally do this in a context listener on application startup
 * <b>after</b> your CDI framework was initialized.
 * </p>
 *
 * @author Patrick Reinhart
 */
@Dependent
public abstract class EventBinder {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventBinder.class);

  private final DeclarerFactory declarerFactory;
  private final Set<QueueBinding<?>> queueBindings;
  private final Set<ExchangeBinding<?>> exchangeBindings;

  @Inject
  private Event<Object> eventSinkBase;
  @Inject
  private EventPublisher eventPublisher;
  @Inject
  private ConnectionRepository connectionRepository;
  @Inject
  private ConsumerContainerFactory consumerContainerFactory;

  private ConnectionConfiguration configuration;
  private ConsumerContainer consumerContainer;
  private DeclarerRepository declarerRepository;

  public EventBinder() {
    exchangeBindings = new HashSet<>();
    queueBindings = new HashSet<>();
    declarerFactory = new DeclarerFactory();
    declarerRepository = new DeclarerRepository();
  }

  /**
   * Extend {@link EventBinder} and implement this method to create the event bindings for your
   * application.
   * <p>
   *
   * <b>Binder example:</b>
   *
   * <pre>
   * public class MyEventBinder extends EventBinder {
   *   &#064;Override
   *   protected void bindEvents() {
   *     bind(MyEvent.class).toExchange(&quot;my.exchange&quot;).withRoutingKey(&quot;my.routing.Key&quot;)
   *         .withDecoder(new MyDecoder());
   *   }
   * }
   * </pre>
   */
  protected abstract void bindEvents();


  /**
   * Returns the configuration object for the event binder, in order to configure the connection
   * specific part.
   * <p>
   *
   * <b>Configuration example:</b>
   *
   * <pre>
   * binder.configuration().setHost("somehost.somedomain").setUsername("user")
   *     .setPassword("password");
   * </pre>
   *
   * @return the configuration object
   */
  public BinderConfiguration configuration() {
    return new BinderConfiguration(configuration);
  }

  /**
   * Returns the DeclarerFactory which can be used to declare exchanges, queues and binding between
   * exchanges and queues. The declaration can later be added to an event binding bia bind()
   * 
   * @see #bind(Class)
   *
   *      <p>
   *
   *      <b>Declaration example:</b>
   *
   *      <pre>
   *      ExchangeDeclaration exchangeDeclaration = declarerFactory()
   *          .createExchangeDeclaration("exchangename").withType(BuiltinExchangeType.FANOUT);
   *      </pre>
   *
   * @return The DeclarerFactory
   */
  public DeclarerFactory declarerFactory() {
    return this.declarerFactory;
  }

  /**
   * Initializes the event binder and effectively enables all bindings created in
   * {@link #bindEvents()}.
   * <p>
   *
   * Inject your event binder implementation at the beginning of your application's life cycle and
   * call this method. In web applications, a good place for this is a ServletContextListener.
   *
   * <p>
   * After this method was successfully called, consumers are registered at the target broker for
   * every queue binding. Also, for every exchange binding messages are going to be published to the
   * target broker.
   *
   * @throws IOException if the initialization failed due to a broker related issue
   */
  public void initialize() throws IOException {
    bindEvents();
    processExchangeBindings();
    processQueueBindings();
    consumerContainer.start();
  }


  /**
   *  Registers a {@link ConnectionListener} to the connection which is used by this object.
   *  <p>
   *  Note: The {@link ConnectionListener} will be added only once, if this method called with the same
   *  {@link ConnectionListener} multiple times.
   *  </p>
   *
   * @param connectionListener the {@link ConnectionListener} which should be registered
   */
  public void registerConnectionListener(ConnectionListener connectionListener) {
    if (!connectionRepository.containsConnectionListener(configuration, connectionListener)) {
      connectionRepository.registerConnectionListener(configuration, connectionListener);
    }
  }

  /**
   *  Removes a {@link ConnectionListener} from the connection which is used by this object.
   *
   * @param connectionListener the {@link ConnectionListener} which should be removed
   */
  public void removeConnectionListener(ConnectionListener connectionListener) {
    connectionRepository.removeConnectionListener(configuration, connectionListener);
  }

  @PostConstruct
  void initializeConsumerContainer() {
    configuration = new ConnectionConfiguration();
    consumerContainer =
        consumerContainerFactory.create(configuration, connectionRepository, declarerRepository);
  }

  @PreDestroy
  void shutdownConsumerContainer() {
    consumerContainer.stop();
  }

  void stop() {
    consumerContainer.stop();
  }

  void processExchangeBindings() {
    exchangeBindings.forEach(this::bindExchange);
    exchangeBindings.clear();
  }

  void processQueueBindings() {
    queueBindings.forEach(this::bindQueue);
    queueBindings.clear();
  }

  void bindQueue(QueueBinding<?> queueBinding) {
    @SuppressWarnings("unchecked")
    QueueBinding<Object> binding = (QueueBinding<Object>) queueBinding;
    Class<Object> eventType = binding.getEventType();
    Decoder<Object> decoder = binding.getDecoder();
    EventConsumer<Object> consumer = new EventConsumer<>(eventType, decoder, eventSinkBase);
    String queue = binding.getQueue();
    consumerContainer.addConsumer(consumer, queue, binding.isAutoAck(), binding.getPrefetchCount(),
        binding.getAllDeclarations());
    LOGGER.info("Binding between queue {} and event type {} activated", queue, eventType.getName());
  }

  <T> void bindExchange(ExchangeBinding<T> exchangeBinding) {
    Class<T> eventType = exchangeBinding.getEventType();
    BiConsumer<T, PublishException> errorHandler = exchangeBinding.getErrorHandler();
    Encoder<T> encoder = exchangeBinding.getEncoder();
    String exchange = exchangeBinding.getExchange();
    PublisherConfiguration<T> cfg = new PublisherConfiguration<T>(configuration, exchange,
        exchangeBinding.routingKeyFunction, exchangeBinding.getBasicPropertiesBuilder(), exchangeBinding.basicPropertiesCalculator, encoder, errorHandler,
        exchangeBinding.getAllDeclarations());
    eventPublisher.addEvent(EventKey.of(eventType, exchangeBinding.getTransactionPhase()), cfg);
    LOGGER.info("Binding between exchange {} and event type {} activated", exchange,
        eventType.getName());
  }

  static <T> BiConsumer<T, PublishException> nop() {
    return (event, cause) -> {
      // no operation
    };
  }

  static String uriDecode(String value) {
    try {
      // URLDecode decodes '+' to a space, as for
      // form encoding. So protect plus signs.
      return URLDecoder.decode(value.replace("+", "%2B"), "US-ASCII");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Starting point for binding an event.
   *
   * <p>
   * <b>Binding fired events to be published to an exchange:</b>
   * <p>
   * bind(MyEvent.class).toExchange("my.exchange");
   * <p>
   * <b>Binding consuming from a queue to fire an event:</b>
   * <p>
   * bind(MyEvent.class).toQueue("my.queue");
   *
   * @param <M> the type of the event
   * @param event The event
   * @return The binding builder
   */
  public <M> EventBindingBuilder<M> bind(Class<M> event) {
    return new EventBindingBuilder<>(event, queueBindings::add, exchangeBindings::add);
  }

  public static final class EventBindingBuilder<T> {
    private final Class<T> eventType;
    private final Consumer<QueueBinding<T>> queueBindingConsumer;
    private final Consumer<ExchangeBinding<T>> exchangeBindingConsumer;

    EventBindingBuilder(Class<T> eventType, Consumer<QueueBinding<T>> queueBindingConsumer,
        Consumer<ExchangeBinding<T>> exchangeBindingConsumer) {
      this.eventType = eventType;
      this.queueBindingConsumer = queueBindingConsumer;
      this.exchangeBindingConsumer = exchangeBindingConsumer;
    }

    /**
     * Binds an event to the given queue. On initialization, a consumer is going to be registered at
     * the broker that is going to fire an event of the bound event type for every consumed message.
     *
     * @param queue The queue
     * @return the queue binding
     */
    public QueueBinding<T> toQueue(String queue) {
      QueueBinding<T> queueBinding = new QueueBinding<>(eventType, queue);
      queueBindingConsumer.accept(queueBinding);
      return queueBinding;
    }

    /**
     * Binds an event to the given exchange. After initialization, all fired events of the bound
     * event type are going to be published to the exchange.
     *
     * @param exchange The exchange
     * @return the exchange binding
     */
    public ExchangeBinding<T> toExchange(String exchange) {
      ExchangeBinding<T> exchangeBinding = new ExchangeBinding<>(eventType, exchange);
      exchangeBindingConsumer.accept(exchangeBinding);
      return exchangeBinding;
    }
  }

  static class DeclarationCollector {
    private final List<BindingDeclaration> bindingDeclarations;
    private final List<ExchangeDeclaration> exchangeDeclarations;
    private final List<QueueDeclaration> queueDeclarations;

    DeclarationCollector() {
      bindingDeclarations = new ArrayList<>();
      exchangeDeclarations = new ArrayList<>();
      queueDeclarations = new ArrayList<>();
    }

    final void add(BindingDeclaration exchangeDeclaration) {
      bindingDeclarations.add(exchangeDeclaration);
    }

    final void add(ExchangeDeclaration exchangeDeclaration) {
      exchangeDeclarations.add(exchangeDeclaration);
    }

    final void add(QueueDeclaration queueDeclaration) {
      queueDeclarations.add(queueDeclaration);
    }

    final List<BindingDeclaration> getBindingDeclarations() {
      return bindingDeclarations;
    }

    final List<ExchangeDeclaration> getExchangeDeclarations() {
      return exchangeDeclarations;
    }

    final List<QueueDeclaration> getQueueDeclarations() {
      return queueDeclarations;
    }

    final List<Declaration> getAllDeclarations(){
      ArrayList<Declaration> allDeclarations = new ArrayList<>();
      allDeclarations.addAll(exchangeDeclarations);
      allDeclarations.addAll(queueDeclarations);
      allDeclarations.addAll(bindingDeclarations);
      return allDeclarations;
    }
  }

  /**
   * Configures and stores the binding between and event class and a queue.
   */
  public static final class QueueBinding<T> extends DeclarationCollector {
    private final Class<T> eventType;
    private final String queue;

    private boolean autoAck;
    private Decoder<T> decoder;
    private int prefetchCount;

    QueueBinding(Class<T> eventType, String queue) {
      this.eventType = eventType;
      this.queue = queue;
      this.decoder = new JsonDecoder<>(eventType);
      this.prefetchCount = 0;
      LOGGER.info("Binding created between queue {} and event type {}", queue,
          eventType.getSimpleName());
    }

    Class<T> getEventType() {
      return eventType;
    }

    String getQueue() {
      return queue;
    }

    boolean isAutoAck() {
      return autoAck;
    }

    Decoder<T> getDecoder() {
      return decoder;
    }

    int getPrefetchCount() {
      return prefetchCount;
    }

    /**
     * <p>
     * Sets the acknowledgement mode to be used for consuming message to automatic acknowledges
     * (auto acks).
     * </p>
     *
     * <p>
     * If auto acks is enabled, messages are delivered by the broker to its consumers in a
     * fire-and-forget manner. The broker removes a message from the queue as soon as its is
     * delivered to the consumer and does not care about whether the consumer successfully processes
     * this message or not.
     * </p>
     *
     * @return the queue binding
     */
    public QueueBinding<T> autoAck() {
      this.autoAck = true;
      LOGGER.info("Auto acknowledges enabled for event type {}", eventType.getSimpleName());
      return this;
    }

    /**
     * Sets the message decoder to be used for message decoding.
     *
     * @param messageDecoder The message decoder instance
     * @return the queue binding
     */
    public QueueBinding<T> withDecoder(Decoder<T> messageDecoder) {
      this.decoder = Objects.requireNonNull(messageDecoder, "decoder must not be null");
      LOGGER.info("Decoder set to {} for event type {}", messageDecoder, eventType.getSimpleName());
      return this;
    }

    /**
     * Adds a queue declaration to this QueueBinding The declaration is automatically applied to the
     * publisher channel
     *
     * @param queueDeclaration The queue declaration
     * @return the queue binding
     */
    public QueueBinding<T> withDeclaration(QueueDeclaration queueDeclaration) {
      add(queueDeclaration);
      return this;
    }

    /**
     * Adds a exchange declaration to this QueueBinding The declaration is automatically applied to
     * the publisher channel
     *
     * @param exchangeDeclaration The exchange declaration
     * @return the queue binding
     */
    public QueueBinding<T> withDeclaration(ExchangeDeclaration exchangeDeclaration) {
      add(exchangeDeclaration);
      return this;
    }

    /**
     * Adds a BindingDeclaration declaration to this QueueBinding The declaration is automatically
     * applied to the publisher channel
     *
     * @param bindingDeclaration The exchange declaration
     * @return the queue binding
     */
    public QueueBinding<T> withDeclaration(BindingDeclaration bindingDeclaration) {
      add(bindingDeclaration);
      return this;
    }

    /**
     * Configures maximum number of unacknowledged buffered messages that are allowed for this
     * channel.
     *
     * @param newPrefetchCount maximum number of messages that will be buffered
     * @return the queue binding
     * @see <a href="https://www.rabbitmq.com/confirms.html#channel-qos-prefetch">Channel Prefetch
     *      Setting (QoS)</a>
     * @see com.rabbitmq.client.Channel#basicQos(int)
     */
    public QueueBinding<T> withPrefetchCount(int newPrefetchCount) {
      this.prefetchCount = newPrefetchCount;
      LOGGER.info("Prefetch count of {} set for event type {}", Integer.valueOf(newPrefetchCount),
          eventType.getSimpleName());
      return this;
    }

    @Override
    public int hashCode() {
      return Objects.hash(eventType, queue);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof QueueBinding)) {
        return false;
      }
      QueueBinding<?> other = (QueueBinding<?>) obj;
      return eventType.equals(other.eventType) && queue.equals(other.queue);
    }

    @Override
    public String toString() {
      return String.format("QueueBinding[type=%s, queue=%s]", eventType.getName(), queue);
    }
  }

  /**
   * Configures and stores the binding between an event class and an exchange.
   */
  public static final class ExchangeBinding<T> extends DeclarationCollector {
    private final Class<T> eventType;
    private final String exchange;
    private final Map<String, Object> headers;

    private Function<T, String> routingKeyFunction;
    private Encoder<T> encoder;
    private Builder basicPropertiesBuilder;
    private TransactionPhase transactionPhase;
    private BiConsumer<T, PublishException> errorHandler;
    private BasicPropertiesCalculator<T> basicPropertiesCalculator;

    ExchangeBinding(Class<T> eventType, String exchange) {
      this.eventType = eventType;
      this.exchange = exchange;
      this.headers = new HashMap<>();
      this.encoder = new JsonEncoder<>();
      routingKeyFunction = e -> "";
      transactionPhase = TransactionPhase.IN_PROGRESS;
      errorHandler = nop();
      basicPropertiesBuilder = MessageProperties.BASIC.builder().headers(headers);
      LOGGER.info("Binding created between exchange {} and event type {}", exchange,
          eventType.getSimpleName());
    }

    Class<T> getEventType() {
      return eventType;
    }

    String getExchange() {
      return exchange;
    }

    String getRoutingKey(T event) {
      return routingKeyFunction.apply(event);
    }

    Encoder<T> getEncoder() {
      return encoder;
    }

    BiConsumer<T, PublishException> getErrorHandler() {
      return errorHandler;
    }

    Builder getBasicPropertiesBuilder() {
      return basicPropertiesBuilder;
    }

    TransactionPhase getTransactionPhase() {
      return transactionPhase;
    }

    BasicPropertiesCalculator<T> getBasicPropertiesCalculator() {
      return basicPropertiesCalculator;
    }

    /**
     * Sets the routing key to be used for message publishing.
     *
     * @param key The routing key
     * @return the exchange binding
     */
    public ExchangeBinding<T> withRoutingKey(String key) {
      Objects.requireNonNull(key, "key must not be null");
      this.routingKeyFunction = e -> key;
      LOGGER.info("Routing key for event type {} set to {}", eventType.getSimpleName(), key);
      return this;
    }

    /**
     * Sets the routing key function to be used for calculating the routing key for message publishing.
     *
     * @param routingKeyFunction The routing key function
     * @return the exchange binding
     */
    public ExchangeBinding<T> withRoutingKey(Function<T, String> routingKeyFunction) {
      this.routingKeyFunction = routingKeyFunction;
      return this;
    }

    /**
     * Sets the message encoder to be used for message encoding.
     *
     * @param messageEncoder The message encoder instance
     * @return the exchange binding
     */
    public ExchangeBinding<T> withEncoder(Encoder<T> messageEncoder) {
      this.encoder = Objects.requireNonNull(messageEncoder, "encoder must not be null");
      LOGGER.info("Encoder for event type {} set to {}", eventType.getSimpleName(),
          encoder.getClass().getName());
      return this;
    }

    /**
     * Sets the message header to the given headerValue to be added when sending each message.
     *
     * @param header the header type
     * @param headerValue the header value
     * @return the exchange binding
     */
    public ExchangeBinding<T> withHeader(String header, Object headerValue) {
      headers.put(Objects.requireNonNull(header, "header must not be null"),
          Objects.requireNonNull(headerValue, "headerValue must not be null"));
      return this;
    }

    /**
     * Sets a function to calculate dynamic properties
     *
     * @param basicPropertiesCalculator the function to calculate the final basic properties including dynamic headers
     * @return the BasicProperties including dynamic headers
     */
    public ExchangeBinding<T> withDynamicProperties(BasicPropertiesCalculator<T> basicPropertiesCalculator) {
      Objects.requireNonNull(basicPropertiesCalculator, "BasicPropertiesCalculator must not be null");
      this.basicPropertiesCalculator = basicPropertiesCalculator;
      return this;
    }

    /**
     * Sets the given basic properties to be used for message publishing. This will reset all
     * previously set headers that may exist.
     *
     * @param properties The basic properties
     * @return the exchange binding
     * @see #withHeader(String, Object)
     */
    public ExchangeBinding<T> withProperties(BasicProperties properties) {
      this.basicPropertiesBuilder = Objects.requireNonNull(properties, "propeties must not be null")
          .builder().headers(headers);
      LOGGER.info("Publisher properties for event type {} set to {}", eventType.getSimpleName(),
          properties.toString());
      headers.clear();
      Map<String, Object> newHeaders = properties.getHeaders();
      if (newHeaders != null) {
        headers.putAll(newHeaders);
      }
      return this;
    }

    /**
     * Sets the event observation phase to the given transaction phase on which the event will be
     * published to the configured exchange.
     *
     * @param phase The transaction phase for the event to publish
     * @return the exchange binding
     */
    public ExchangeBinding<T> inPhase(TransactionPhase phase) {
      this.transactionPhase = Objects.requireNonNull(phase, "phase must not be null");
      return this;
    }

    /**
     * Sets the given error handler to be used when a event could not be published to RabbitMQ.
     *
     * @param handler The custom error handler
     * @return the exchange binding
     */
    public ExchangeBinding<T> withErrorHandler(BiConsumer<T, PublishException> handler) {
      this.errorHandler = handler == null ? nop() : handler;
      return this;
    }


    /**
     * Adds a queue declaration to this ExchangeBinding The declaration is automatically applied to
     * the consumer channel
     *
     * @param queueDeclaration The queue declaration
     * @return the queue binding
     */
    public ExchangeBinding<T> withDeclaration(QueueDeclaration queueDeclaration) {
      add(queueDeclaration);
      return this;
    }

    /**
     * Adds a exchange declaration to this ExchangeBinding The declaration is automatically applied
     * to the consumer channel
     *
     * @param exchangeDeclaration The exchange declaration
     * @return the queue binding
     */
    public ExchangeBinding<T> withDeclaration(ExchangeDeclaration exchangeDeclaration) {
      add(exchangeDeclaration);
      return this;
    }

    /**
     * Adds a BindingDeclaration declaration to this ExchangeBinding The declaration is
     * automatically applied to the consumer channel
     *
     * @param bindingDeclaration The exchange declaration
     * @return the queue binding
     */
    public ExchangeBinding<T> withDeclaration(BindingDeclaration bindingDeclaration) {
      add(bindingDeclaration);
      return this;
    }

    @Override
    public int hashCode() {
      return Objects.hash(eventType, exchange);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof ExchangeBinding)) {
        return false;
      }
      ExchangeBinding<?> other = (ExchangeBinding<?>) obj;
      return eventType.equals(other.eventType) && exchange.equals(other.exchange);
    }

    @Override
    public String toString() {
      return String.format("ExchangeBinding[type=%s, exchange=%s]", eventType.getName(), exchange);
    }

  }

  public final static class BinderConfiguration {
    private final ConnectionConfigHolder config;

    BinderConfiguration(ConnectionConfigHolder config) {
      this.config = config;
    }

    /**
     * Adds the given {@code hostName} to the set of hostnames, taken when connecting.
     *
     * @param hostName the RabbitMQ host name to use
     * @return the binder configuration object
     * @deprecated Use {@link #addHost(String)} instead
     */
    @Deprecated
    public BinderConfiguration setHost(String hostName) {
      return addHost(Address.parseAddress(hostName));
    }

    /**
     * Adds a broker host name used when establishing a connection.
     *
     * @param hostName a broker host name with optional port
     * @return the binder configuration object
     */
    public BinderConfiguration addHost(String hostName) {
      return addHost(Address.parseAddress(hostName));
    }

    /**
     * Adds a broker host address used when establishing a connection.
     *
     * @param hostAddress the broker host address
     * @return the binder configuration object
     */
    public BinderConfiguration addHost(Address hostAddress) {
      config.addHost(hostAddress);
      return this;
    }

    /**
     * Set the user name.
     *
     * @param username the AMQP user name to use when connecting to the broker
     * @return the binder configuration object
     */
    public BinderConfiguration setUsername(String username) {
      config.setUsername(username);
      return this;
    }

    /**
     * Set the password.
     *
     * @param password the password to use when connecting to the broker
     * @return the binder configuration object
     */
    public BinderConfiguration setPassword(String password) {
      config.setPassword(password);
      return this;
    }

    /**
     * Set the virtual host.
     *
     * @param virtualHost the virtual host to use when connecting to the broker
     * @return the binder configuration object
     */
    public BinderConfiguration setVirtualHost(String virtualHost) {
      config.setVirtualHost(virtualHost);
      return this;
    }

    /**
     * Set the time to sleep between retries to activate consumers
     *
     * @param waitTime time in milli seconds to wait between retries
     * @return the binder configuration object
     */
    public BinderConfiguration setFailedConsumerActivationRetryTime(long waitTime) {
      config.setFailedConsumerActivationRetryTime(waitTime);
      return this;
    }

    /**
     * Set the connection security setting.
     *
     * @param secure {@code true} to use secured connection, {@code false} otherwise
     * @return the binder configuration object
     */
    public BinderConfiguration setSecure(boolean secure) {
      config.setSecure(secure);
      return this;
    }

    /**
     * Set the connection parameters using the given {@code uri}. This will reset all other
     * settings.
     *
     * @param uri the connection URI
     * @return the binder configuration object
     */
    public BinderConfiguration setConnectionUri(URI uri) {
      int port = uri.getPort();
      String scheme = uri.getScheme().toLowerCase();
      if ("amqp".equals(scheme)) {
        // nothing special to do
        if (port == -1) {
          port = ConnectionFactory.DEFAULT_AMQP_PORT;
        }
      } else if ("amqps".equals(scheme)) {
        config.setSecure(true);
        if (port == -1) {
          port = ConnectionFactory.DEFAULT_AMQP_OVER_SSL_PORT;
        }
      } else {
        throw new IllegalArgumentException("Wrong scheme in AMQP URI: " + uri.getScheme());
      }

      String host = uri.getHost();
      if (host == null) {
        host = "127.0.0.1";
      }
      config.setHosts(Collections.singleton(new Address(host, port)));

      String userInfo = uri.getRawUserInfo();
      if (userInfo != null) {
        String userPass[] = userInfo.split(":");
        if (userPass.length > 2) {
          throw new IllegalArgumentException("Bad user info in AMQP URI: " + userInfo);
        }

        setUsername(uriDecode(userPass[0]));
        if (userPass.length == 2) {
          setPassword(uriDecode(userPass[1]));
        }
      }

      String path = uri.getRawPath();
      if (path != null && path.length() > 0) {
        if (path.indexOf('/', 1) != -1) {
          throw new IllegalArgumentException("Multiple segments in path of AMQP URI: " + path);
        }

        setVirtualHost(uriDecode(path.substring(1)));
      }
      return this;
    }

    /**
     * Set the TCP connection timeout.
     *
     * @param timeout connection TCP establishment timeout in milliseconds; zero for infinite
     * @return the binder configuration object
     */
    public BinderConfiguration setConnectTimeout(int timeout) {
      config.setConnectTimeout(timeout);
      return this;
    }

    /**
     * Set the time to sleep between connection attempts, this only applies if the connection was
     * not recoverable and a complete reconnect is needed and also during the first connect attempt.
     *
     * @param waitTime time in milli seconds to wait between retries
     * @return the binder configuration object
     */
    public BinderConfiguration setConnectRetryWaitTime(int waitTime) {
      config.setConnectRetryWaitTime(waitTime);
      return this;
    }

    /**
     * Set the requested heartbeat timeout. Heartbeat frames will be sent at about 1/2 the timeout
     * interval. If server heartbeat timeout is configured to a non-zero value, this method can only
     * be used to lower the value; otherwise any value provided by the client will be used.
     *
     * @param requestedHeartbeat the initially requested heartbeat timeout, in seconds; zero for
     *        none
     * @return the binder configuration object
     * @see <a href="http://rabbitmq.com/heartbeats.html">RabbitMQ Heartbeats Guide</a>
     */
    public BinderConfiguration setRequestedConnectionHeartbeatTimeout(int requestedHeartbeat) {
      config.setRequestedConnectionHeartbeatTimeout(requestedHeartbeat);
      return this;
    }

  }
}
