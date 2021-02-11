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

import java.util.EventListener;

import com.rabbitmq.client.Connection;

/**
 * A connection listener is used by a connection factory to notify clients about a change in
 * connection state.
 * 
 * @author Patrick Reinhart
 */
public interface ConnectionListener extends EventListener {

  /**
   * Called when a connection was established the first time.
   * 
   * @param connection The established connection
   */
  void onConnectionEstablished(Connection connection);

  /**
   * Called when a connection was lost and the connection factory is trying to reestablish the
   * connection.
   * 
   * @param connection The lost connection
   */
  void onConnectionLost(Connection connection);

  /**
   * Called when a connection was ultimately closed and no new connection is going to be established
   * in the future (this the case if the connection factory was teared down).
   * 
   * @param connection The closed connection
   */
  void onConnectionClosed(Connection connection);

}
