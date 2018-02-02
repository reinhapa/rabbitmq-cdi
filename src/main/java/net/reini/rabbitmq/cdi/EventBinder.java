package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Address;
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
@Singleton
public abstract class EventBinder {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventBinder.class);

  private final Set<QueueBinding<?>> queueBindings;
  private final Set<ExchangeBinding<?>> exchangeBindings;

  @Inject
  private Event<Object> remoteEventControl;
  @Inject
  private Instance<Object> remoteEventPool;
  @Inject
  private EventPublisher eventPublisher;
  @Inject
  private ConnectionProducer connectionProducer;

  private ConsumerContainer consumerContainer;
  private BinderConfiguration configuration;

  public EventBinder() {
    exchangeBindings = new HashSet<>();
    queueBindings = new HashSet<>();
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
    return configuration;
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
    processQueueBindings();
    consumerContainer.startAllConsumers();
    processExchangeBindings();
  }


  @PostConstruct
  void initializeConsumerContainer() {
    consumerContainer = new ConsumerContainer(connectionProducer);
    configuration = new BinderConfiguration();
  }

  void processExchangeBindings() {
    for (ExchangeBinding<?> exchangeBinding : exchangeBindings) {
      bindExchange(exchangeBinding);
    }
    exchangeBindings.clear();
  }

  void processQueueBindings() {
    for (QueueBinding<?> queueBinding : queueBindings) {
      bindQueue(queueBinding);
    }
    queueBindings.clear();
  }

  void bindQueue(QueueBinding<?> queueBinding) {
    @SuppressWarnings("unchecked")
    Event<Object> eventControl = (Event<Object>) remoteEventControl.select(queueBinding.eventType);
    @SuppressWarnings("unchecked")
    Instance<Object> eventPool = (Instance<Object>) remoteEventPool.select(queueBinding.eventType);
    EventConsumer consumer =
        new EventConsumer(queueBinding.decoder, queueBinding.autoAck, eventControl, eventPool);
    consumerContainer.addConsumer(consumer, queueBinding.queue, queueBinding.autoAck);
    LOGGER.info("Binding between queue {} and event type {} activated", queueBinding.queue,
        queueBinding.eventType.getSimpleName());
  }

  void bindExchange(ExchangeBinding<?> exchangeBinding) {
    PublisherConfiguration cfg =
        new PublisherConfiguration(exchangeBinding.exchange, exchangeBinding.routingKey,
            exchangeBinding.basicPropertiesBuilder, exchangeBinding.encoder, connectionProducer);
    eventPublisher.addEvent(exchangeBinding.eventType, cfg);
    LOGGER.info("Binding between exchange {} and event type {} activated", exchangeBinding.exchange,
        exchangeBinding.eventType.getSimpleName());
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
   * @param event The event
   * @return The binding builder
   */
  public <M> EventBindingBuilder<M> bind(Class<M> event) {
    return new EventBindingBuilder<>(event);
  }

  public final class EventBindingBuilder<T> {
    private final Class<T> eventType;

    EventBindingBuilder(Class<T> eventType) {
      this.eventType = eventType;
    }

    /**
     * Binds an event to the given queue. On initialization, a consumer is going to be registered at
     * the broker that is going to fire an event of the bound event type for every consumed message.
     *
     * @param queue The queue
     * @return the queue binding
     */
    public QueueBinding<T> toQueue(String queue) {
      return new QueueBinding<>(eventType, queue);
    }

    /**
     * Binds an event to the given exchange. After initialization, all fired events of the bound
     * event type are going to be published to the exchange.
     *
     * @param exchange The exchange
     * @return the exchange binding
     */
    public ExchangeBinding<T> toExchange(String exchange) {
      return new ExchangeBinding<>(eventType, exchange);
    }
  }

  /**
   * Configures and stores the binding between and event class and a queue.
   */
  public final class QueueBinding<T> {
    private final Class<T> eventType;
    private final String queue;
    private boolean autoAck;
    private Decoder<T> decoder;

    QueueBinding(Class<T> eventType, String queue) {
      this.eventType = eventType;
      this.queue = queue;
      this.decoder = new JsonDecoder<>(eventType);
      queueBindings.add(this);
      LOGGER.info("Binding created between queue {} and event type {}", queue,
          eventType.getSimpleName());
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
  }

  /**
   * Configures and stores the binding between an event class and an exchange.
   */
  public final class ExchangeBinding<T> {
    private final Class<T> eventType;
    private final String exchange;

    private String routingKey;
    private Encoder<T> encoder;
    private Builder basicPropertiesBuilder;

    ExchangeBinding(Class<T> eventType, String exchange) {
      this.eventType = eventType;
      this.exchange = exchange;
      this.encoder = new JsonEncoder<>();
      basicPropertiesBuilder = MessageProperties.BASIC.builder();
      exchangeBindings.add(this);
      LOGGER.info("Binding created between exchange {} and event type {}", exchange,
          eventType.getSimpleName());
    }

    /**
     * Sets the routing key to be used for message publishing.
     *
     * @param key The routing key
     * @return the exchange binding
     */
    public ExchangeBinding<T> withRoutingKey(String key) {
      this.routingKey = Objects.requireNonNull(key, "key must not be null");
      LOGGER.info("Routing key for event type {} set to {}", eventType.getSimpleName(), key);
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
     * Sets the given basic properties to be used for message publishing.
     *
     * @param properties The basic properties
     * @return the exchange binding
     */
    public ExchangeBinding<T> withProperties(BasicProperties properties) {
      this.basicPropertiesBuilder =
          Objects.requireNonNull(properties, "propeties must not be null").builder();
      LOGGER.info("Publisher properties for event type {} set to {}", eventType.getSimpleName(),
          properties.toString());
      return this;
    }
  }

  public final class BinderConfiguration {

    /**
     * Adds a broker host name used when establishing a connection.
     * 
     * @param hostName a broker host name without a port
     * @return the binder configuration object
     */
    public BinderConfiguration setHost(String hostName) {
      connectionProducer.getConnectionFactory().setHost(hostName);
      return this;
    }

    /**
     * Set the user name.
     * 
     * @param username the AMQP user name to use when connecting to the broker
     * @return the binder configuration object
     */
    public BinderConfiguration setUsername(String username) {
      connectionProducer.getConnectionFactory().setUsername(username);
      return this;
    }

    /**
     * Set the password.
     * 
     * @param password the password to use when connecting to the broker
     * @return the binder configuration object
     */
    public BinderConfiguration setPassword(String password) {
      connectionProducer.getConnectionFactory().setPassword(password);
      return this;
    }

    /**
     * Set the virtual host.
     * 
     * @param virtualHost the virtual host to use when connecting to the broker
     * @return the binder configuration object
     */
    public BinderConfiguration setVirtualHost(String virtualHost) {
      connectionProducer.getConnectionFactory().setVirtualHost(virtualHost);
      return this;
    }

    /**
     * Adds a broker host address used when establishing a connection.
     * 
     * @param hostAddress the broker host address
     * @return the binder configuration object
     */
    public BinderConfiguration addHost(Address hostAddress) {
      connectionProducer.getBrokerHosts().add(hostAddress);
      return this;
    }

    /**
     * Register a <code>ConnectionListener</code> for monitoring connection.
     *
     * @param listener the listener for the connection
     * @return the binder configuration object
     */
    public BinderConfiguration registerListener(ConnectionListener listener) {
      connectionProducer.registerListener( listener );
      return this;
    }

  }
}
