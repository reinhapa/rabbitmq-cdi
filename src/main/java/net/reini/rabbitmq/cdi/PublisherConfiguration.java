/**
 * File Name: PublisherConfiguration.java
 * 
 * Copyright (c) 2015 BISON Schweiz AG, All Rights Reserved.
 */

package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.AMQP;

/**
 * A publisher configuration stores all important settings and options used for publishing and
 * event.
 *
 * @author Patrick Reinhart
 * @param <T> Message type
 */
final class PublisherConfiguration {
  PublisherConfiguration(String exchange, String routingKey, boolean persistent,
      AMQP.BasicProperties basicProperties) {
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.persistent = persistent;
    this.basicProperties = basicProperties;
    this.messageEncoder = new JsonEncoder<>();
  }
  
  PublisherConfiguration(String exchange, String routingKey, boolean persistent,
          AMQP.BasicProperties basicProperties, Encoder<?> encoder) {
    this.exchange = exchange;
    this.routingKey = routingKey;
    this.persistent = persistent;
    this.basicProperties = basicProperties;
    this.messageEncoder = encoder;
  }

  final boolean persistent;
  final String exchange;
  final String routingKey;
  final AMQP.BasicProperties basicProperties;
  final Encoder<?> messageEncoder;
}
