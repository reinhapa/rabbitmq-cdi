package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

class ConsumerChannelFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerChannelFactory.class);
    private final ConnectionRepository connectionRepository;
    private final ConnectionConfiguration config;

    public ConsumerChannelFactory(ConnectionRepository connectionRepository, ConnectionConfiguration config)
    {

        this.connectionRepository = connectionRepository;
        this.config = config;
    }

    /**
     * Creates a channel to be used for consuming from the broker.
     *
     * @return The channel
     * @throws IOException              if the channel cannot be created due to a connection problem
     * @throws TimeoutException         if the channel cannot be created due to a timeout problem
     * @throws NoSuchAlgorithmException if the security context creation for secured connection fails
     */
    protected Channel createChannel() throws IOException
    {
        LOGGER.debug("Creating channel");
        Connection connection = connectionRepository.getConnection(config);
        Channel channel = connection.createChannel();
        LOGGER.debug("Created channel");
        return channel;
    }

}
