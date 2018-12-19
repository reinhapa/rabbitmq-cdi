package net.reini.rabbitmq.cdi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ResourceCloserTest
{
    private ResourceCloser sut = new ResourceCloser();
    @Mock
    private AutoCloseable autoCloseableMock;

    @Test
    void testCloseNull()
    {
        sut.closeResource(null,"message");
    }

    @Test
    void testCloseMock() throws Exception
    {
        sut.closeResource(autoCloseableMock,"message");
        verify(autoCloseableMock).close();
    }

    @Test
    void testCloseMockWithException() throws Exception
    {
        Mockito.doThrow(new IOException()).when(autoCloseableMock).close();
        sut.closeResource(autoCloseableMock,"message");
        verify(autoCloseableMock).close();
    }

}