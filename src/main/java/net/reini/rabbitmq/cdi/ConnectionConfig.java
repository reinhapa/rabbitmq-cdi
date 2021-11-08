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
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Function responsible to create new connections using a given connection factory.
 *
 * @author Patrick Reinhart
 */
public interface ConnectionConfig {
  /**
   * @param connectionFactory the connection factory used to create the actual connection
   * @return a new connection using the given connection factory
   * 
   * @throws TimeoutException if a timeout occurs
   * @throws IOException if the connection error occurs
   */
  Connection createConnection(ConnectionFactory connectionFactory)
      throws IOException, TimeoutException;

  /**
   * @return the wait time between connection retry
   */
  long getConnectRetryWaitTime();

  /**
   * @return the wait time between consumers failed to activate will be tried to restart
   */
  long getFailedConsumerActivationRetryTime();
}
