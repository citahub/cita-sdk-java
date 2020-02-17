package com.citahub.cita.protocol.ipc;

import java.io.IOException;

import com.citahub.cita.protocol.core.methods.response.CITAClientVersion;
import org.junit.Before;
import org.junit.Test;

import com.citahub.cita.protocol.core.Request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IpcServiceTest {

    private IpcService ipcService;
    private IOFacade ioFacade;

    @Before
    public void setUp() {
        ioFacade = mock(IOFacade.class);
        ipcService = new IpcService(ioFacade);
    }

    @Test
    public void testSend() throws IOException {
        when(ioFacade.read()).thenReturn(
                "{\"jsonrpc\":\"2.0\",\"id\":1,"
                        + "\"result\":\"Geth/v1.5.4-stable-b70acf3c/darwin/go1.7.3\"}\n");

        ipcService.send(new Request(), CITAClientVersion.class);

        verify(ioFacade).write("{\"jsonrpc\":\"2.0\",\"method\":null,\"params\":null,\"id\":0}");
    }
}
