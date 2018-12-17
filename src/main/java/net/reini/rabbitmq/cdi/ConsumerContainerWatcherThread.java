package net.reini.rabbitmq.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerContainerWatcherThread extends Thread
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainerWatcherThread.class);
    private final ConsumerContainer consumerContainer;
    private long retryTime;

    public ConsumerContainerWatcherThread(ConsumerContainer consumerContainer, long retryTime)
    {
        this.consumerContainer = consumerContainer;
        this.retryTime = retryTime;
        this.setDaemon(true);
        this.setName("consumer watcher thread");
    }

    @Override
    public void run()
    {
        while (Thread.currentThread().isInterrupted() == false)
        {
            boolean allConsumersActive = false;
            synchronized (consumerContainer)
            {

                if (consumerContainer.isConnectionAvailable())
                {
                    allConsumersActive = consumerContainer.ensureConsumersAreActive();
                }
                if (allConsumersActive || consumerContainer.isConnectionAvailable() == false)
                {
                    try
                    {
                        consumerContainer.wait();
                    }
                    catch (InterruptedException e)
                    {
                        LOGGER.info("interrupted while waiting for notification");
                        Thread.currentThread().interrupt();
                    }
                }
            }
            if (allConsumersActive == false)
                {
                    LOGGER.warn("could not activate all consumer. Retry to activate failed consumers");
                    try
                    {
                        Thread.sleep(retryTime);
                    }
                    catch (InterruptedException e)
                    {
                        LOGGER.info("interrupted while sleeping",e);
                        Thread.currentThread().interrupt();
                    }
                }

        }
    }


}