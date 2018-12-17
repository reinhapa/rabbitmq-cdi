package net.reini.rabbitmq.cdi;

import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection;

public class UnrecoverableErrorDetector
{
    public boolean isUnrecoverableError(ShutdownSignalException cause)
    {
        return !AutorecoveringConnection.DEFAULT_CONNECTION_RECOVERY_TRIGGERING_CONDITION.test(cause);
    }

}
