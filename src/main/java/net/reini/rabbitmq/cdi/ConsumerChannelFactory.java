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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.RecoverableChannel;

class ConsumerChannelFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerChannelFactory.class);

  private final ConnectionRepository connectionRepository;
  private final ConnectionConfig config;

  ConsumerChannelFactory(ConnectionRepository connectionRepository, ConnectionConfig config) {
    this.connectionRepository = connectionRepository;
    this.config = config;
  }

  /**
   * Creates a channel to be used for consuming from the broker.
   *
   * @return The channel
   * @throws IOException if the channel cannot be created due to a connection problem
   */
  protected RecoverableChannel createChannel() throws IOException {
    LOGGER.debug("Creating channel");
    Connection connection = connectionRepository.getConnection(config);
    RecoverableChannel channel = (RecoverableChannel) connection.createChannel();
    LOGGER.debug("Created channel");
    return channel;
  }
}
