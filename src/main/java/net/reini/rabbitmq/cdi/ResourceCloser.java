package net.reini.rabbitmq.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResourceCloser {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCloser.class);

  void closeResource(AutoCloseable autoCloseable, String logTextIfFailed) {
    try {
      if (autoCloseable != null) {
        autoCloseable.close();
      }
    } catch (Exception e) {
      LOGGER.warn(logTextIfFailed);
      LOGGER.debug(logTextIfFailed, e);
    }
  }
}
