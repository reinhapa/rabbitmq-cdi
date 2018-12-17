package net.reini.rabbitmq.cdi;

enum ConnectionState {
  /**
   * The factory has never established a connection so far.
   */
  NEVER_CONNECTED,
  /**
   * The factory has established a connection in the past but the connection was lost and the factory is currently trying to reestablish the connection.
   */
  CONNECTING,
  /**
   * The factory has established a connection that is currently alive and that can be retrieved.
   */
  CONNECTED,
  /**
   * The factory and its underlying connection are closed and the factory cannot be used to retrieve new connections.
   */
  CLOSED
}
