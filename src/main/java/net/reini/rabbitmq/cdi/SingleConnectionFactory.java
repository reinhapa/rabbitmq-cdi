package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * <p>
 * A single connection factory provides ONE SINGLE connection to a RabbitMQ
 * message broker via TCP.
 * </p>
 * 
 * <p>
 * It is recommended by the RabbitMQ documentation (v2.7) to use one single
 * connection within a client and to use one channel for every client thread.
 * </p>
 * 
 * @author Patrick Reinhart
 */
@Singleton
public class SingleConnectionFactory extends ConnectionFactory {

    private enum State {
	/**
	 * The factory has never established a connection so far.
	 */
	NEVER_CONNECTED, /**
	 * The factory has established a connection in the past
	 * but the connection was lost and the factory is currently trying to
	 * reestablish the connection.
	 */
	CONNECTING, /**
	 * The factory has established a connection that is
	 * currently alive and that can be retrieved.
	 */
	CONNECTED, /**
	 * The factory and its underlying connection are closed and
	 * the factory cannot be used to retrieve new connections.
	 */
	CLOSED
    }

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(SingleConnectionFactory.class);

    public static final int CONNECTION_HEARTBEAT_IN_SEC = 3;
    public static final int CONNECTION_TIMEOUT_IN_MS = 1000;
    public static final int CONNECTION_ESTABLISH_INTERVAL_IN_MS = 500;

    private final ShutdownListener connectionShutdownListener;
    private final List<ConnectionListener> connectionListeners;
    private final Object operationOnConnectionMonitor;

    private volatile Connection connection;
    private volatile State state;

    public SingleConnectionFactory() {
	super();
	state = State.NEVER_CONNECTED;
	operationOnConnectionMonitor = new Object();
	setRequestedHeartbeat(CONNECTION_HEARTBEAT_IN_SEC);
	setConnectionTimeout(CONNECTION_TIMEOUT_IN_MS);
	connectionListeners = Collections.synchronizedList(new LinkedList<>());
	connectionShutdownListener = new ConnectionShutDownListener();
    }

    /**
     * <p>
     * Gets a new connection from the factory. As this factory only provides one
     * connection for every process, the connection is established on the first
     * call of this method. Every subsequent call will return the same instance
     * of the first established connection.
     * </p>
     * 
     * <p>
     * In case a connection is lost, the factory will try to reestablish a new
     * connection.
     * </p>
     * 
     * @return The connection
     */
    @Override
    public Connection newConnection() throws IOException, TimeoutException {
	// Throw an exception if there is an attempt to retrieve a connection
	// from a closed factory
	if (state == State.CLOSED) {
	    throw new IOException(
		    "Attempt to retrieve a connection from a closed connection factory");
	}
	// Try to establish a connection if there was no connection attempt so
	// far
	if (state == State.NEVER_CONNECTED) {
	    establishConnection();
	}
	// Retrieve the connection if it is established
	if (connection != null && connection.isOpen()) {
	    return connection;
	}
	// Throw an exception if no established connection could not be
	// retrieved
	LOGGER.error("Unable to retrieve connection");
	throw new IOException("Unable to retrieve connection");
    }

    /**
     * <p>
     * Closes the connection factory and interrupts all threads associated to
     * it.
     * </p>
     * 
     * <p>
     * Note: Make sure to close the connection factory when not used any more as
     * otherwise the connection may remain established and ghost threads may
     * reside.
     * </p>
     */
    @PreDestroy
    public void close() {
	synchronized (operationOnConnectionMonitor) {
	    if (state == State.CLOSED) {
		LOGGER.warn("Attempt to close connection factory which is already closed");
		return;
	    }
	    LOGGER.info("Closing connection factory");
	    if (connection != null) {
		try {
		    connection.close();
		    connection = null;
		} catch (IOException e) {
		    if (!connection.isOpen()) {
			LOGGER.warn("Attempt to close an already closed connection");
		    } else {
			LOGGER.error("Unable to close current connection", e);
		    }
		}
	    }
	    changeState(State.CLOSED);
	    LOGGER.info("Closed connection factory");
	}
    }

    /**
     * Registers a connection listener at the factory which is notified about
     * changes of connection states.
     * 
     * @param connectionListener
     *            The connection listener
     */
    public void registerListener(ConnectionListener connectionListener) {
	connectionListeners.add(connectionListener);
    }

    /**
     * Removes a connection listener from the factory.
     *
     * @param connectionListener
     *            The connection listener
     */
    public void removeConnectionListener(ConnectionListener connectionListener) {
	connectionListeners.remove(connectionListener);
    }

    /**
     * Changes the factory state and notifies all connection listeners.
     *
     * @param newState
     *            The new connection factory state
     */
    void changeState(State newState) {
	state = newState;
	notifyListenersOnStateChange();
    }

    /**
     * Notifies all connection listener about a state change.
     */
    void notifyListenersOnStateChange() {
	LOGGER.debug("Notifying connection listeners about state change to {}",
		state);

	for (ConnectionListener listener : connectionListeners) {
	    switch (state) {
	    case CONNECTED:
		listener.onConnectionEstablished(connection);
		break;
	    case CONNECTING:
		listener.onConnectionLost(connection);
		break;
	    case CLOSED:
		listener.onConnectionClosed(connection);
		break;
	    default:
		break;
	    }
	}
    }

    /**
     * Establishes a new connection.
     *
     * @throws IOException
     *             if establishing a new connection fails
     * @throws TimeoutException
     *             if establishing a new connection times out
     */
    void establishConnection() throws IOException, TimeoutException {
	synchronized (operationOnConnectionMonitor) {
	    if (state == State.CLOSED) {
		throw new IOException(
			"Attempt to establish a connection with a closed connection factory");
	    } else if (state == State.CONNECTED) {
		LOGGER.warn("Establishing new connection although a connection is already established");
	    }
	    Integer port = Integer.valueOf(getPort());
	    String host = getHost();
	    try {
		LOGGER.info("Trying to establish connection to {}:{}", host,
			port);
		connection = super.newConnection();
		connection.addShutdownListener(connectionShutdownListener);
		LOGGER.info("Established connection to {}:{}", host, port);
		changeState(State.CONNECTED);
	    } catch (IOException e) {
		LOGGER.error("Failed to establish connection to {}:{}", host,
			port);
		throw e;
	    }
	}
    }

    /**
     * A listener to register on the parent factory to be notified about
     * connection shutdowns.
     */
    private class ConnectionShutDownListener implements ShutdownListener {
	@Override
	public void shutdownCompleted(ShutdownSignalException cause) {
	    // Only hard error means loss of connection
	    if (!cause.isHardError()) {
		return;
	    }

	    synchronized (operationOnConnectionMonitor) {
		// No action to be taken if factory is already closed
		// or already connecting
		if (state == State.CLOSED || state == State.CONNECTING) {
		    return;
		}
		changeState(State.CONNECTING);
	    }
	    LOGGER.error("Connection to {}:{} lost", getHost(),
		    Integer.valueOf(getPort()));
	    while (state == State.CONNECTING) {
		try {
		    establishConnection();
		    return;
		} catch (IOException | TimeoutException e) {
		    LOGGER.info("Next reconnect attempt in {} ms", Integer
			    .valueOf(CONNECTION_ESTABLISH_INTERVAL_IN_MS));
		    try {
			Thread.sleep(CONNECTION_ESTABLISH_INTERVAL_IN_MS);
		    } catch (InterruptedException ie) {
			// that's fine, simply stop here
			return;
		    }
		}
	    }
	}
    }
}