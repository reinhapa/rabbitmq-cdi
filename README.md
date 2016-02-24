# Provides a JavaEE Event <-> RabbitMQ bridge.

This project contains all needed classes to bind a JavaEE enterprise event to a
RabbitMQ exchange for outgoing events. Inbound events can also be bound on the
respective queues and will be hand over to all JavaEE event observers.

The RabbitMQ message content is done via JSON serialization of a Java Bean 
compatible PoJo object and vice versa.

## Usage example

First you need to define a event objects using standard Java Bean syntax:

```
public class EventOne {
  private boolean enabled;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
```
```
public class EventTwo {
  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
```


As second step you have to define the bindings:

```
public class RabbitBinder extends EventBinder {
  @Override
  protected void bindEvents() {
    bind(EventOne.class).toExchange("test.from").withRoutingKey("test.key");
    bind(EventTwo.class).toQueue("test.queue");
  }
}
```

As last step you need to initialize the bindings either within in a singleton
startup bean or servlet:

```
@Singleton
@Startup
public class BindingInitializer {
  @Inject
  private RabbitBinder binder;
  @Inject
  private ConnectionFactory connectionFactory;

  @PostConstruct
  public void initialize() {
    try {
      connectionFactory.setHost("somehost.somedomain");
      binder.initialize();
    } catch (IOException e) {
      LoggerFactory.getLogger(getClass()).error("Unable to initialize", e);
    }
  }
}
```


Now the events can be used within your JavaEE 7 container:

```
public class EventDemoBean {
  @Inject
  private Event<EventOne> eventOnes;
  
  public void submitEvent(boolean enabled) {
    EventOne eventOne = new EventOne();
    eventOne.setEnabled(enabled);
    eventOnes.fire(eventOne);
  }

  public void reveiveEvent(@Observes EventTwo eventTwo) {
    String data = eventTwo.getData();
    // do some work
  }
}
```

