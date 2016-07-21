package net.reini.rabbitmq.cdi;

/**
 * Abstract message converter to transform a object in a array de bytes to send a message.
 * It is recomended implement this class to crete your custom message converter.
 * 
 * @author André Ignacio
 */
public abstract class AbstractMessageConverter implements MessageConverter {

    /**
     * Transform a object in a array de bytes to send a message.
     * 
     * @return Array of bytes
     * @throws MessageConverterException If the conversion fails
     */
    public byte[] toBytes(Object object) throws MessageConverterException{
        try {
            return toBytesInner(object);
        } catch (Exception e) {
            throw new MessageConverterException(e);
        }
    }
    
    public abstract byte[] toBytesInner(Object object) throws Exception;
}
