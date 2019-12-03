package com.citahub.cita.tests;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.Request;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.AppFilter;
import com.citahub.cita.protocol.core.methods.response.AppLog;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class BlockFilterTransactionTest {
    private static CITAj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
    }

    @Test
    public void testBlockFilterTransaction( ) throws IOException, InterruptedException {
        Request<?, AppFilter> request = service.appNewBlockFilter();
        AppFilter appFilter = request.send();
        BigInteger filterId = appFilter.getFilterId();

        TimeUnit.SECONDS.sleep(15);

        AppLog appLog
                = service.appGetFilterChanges(filterId).send();

        List<AppLog.LogResult> logResults = appLog.getLogs();


        System.out.println(logResults.size());
        for (AppLog.LogResult logResult : logResults) {
            Object s = logResult.get();
            System.out.println("Block Hash" + s.toString());
            AppBlock block = service.appGetBlockByHash(s.toString(), true).send();
            assertNull(block.getError());
            assertNotNull(block.getBlock().getHeader().getNumber());
            assertTrue(block.getBlock().getHash().equals(s.toString()));
            System.out.println("Block number: " + block.getBlock().getHeader().getNumber() + "\n");
        }
    }
}
