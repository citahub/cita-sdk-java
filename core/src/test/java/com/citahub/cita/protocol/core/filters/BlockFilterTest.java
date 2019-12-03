package com.cryptape.cita.protocol.core.filters;

import com.cryptape.cita.protocol.core.methods.response.AppLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BlockFilterTest extends FilterTester {

    @Test
    public void testBlockFilter() throws Exception {

        AppLog appLog = objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":["
                        + "\"0x31c2342b1e0b8ffda1507fbffddf213c4b3c1e819ff6a84b943faabb0ebf2403\","
                        + "\"0xccc0d2e07c1febcaca0c3341c4e1268204b06fefa4bb0c8c0d693d8e581c82da\""
                        + "]}",
                AppLog.class);

        runTest(appLog, citaj.appBlockHashFlowable());
    }
}
