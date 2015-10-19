# Provides a JavaEE Event <-> RabbitMQ bridge.

This project contains all needed classes to bind a JavaEE enterprise event to a
RabbitMQ exchange for outgoing events. Inbound events can also be bound on the
respective queues and will be hand over to all JavaEE event observers.

The RabbitMQ message content is done via JSON serialization of a Java Bean 
compatible PoJo object and vice versa.
