package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.protocol.core.methods.response.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppLog;

public class BlockFilterTransactionExample {
    private static AppChainj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Request<?, AppFilter> request = service.appNewBlockFilter();
        AppFilter appFilter = request.send();
        BigInteger filterId = appFilter.getFilterId();

        TimeUnit.SECONDS.sleep(15);

        org.nervos.appchain.protocol.core.methods.response.AppLog appLog
                = service.appGetFilterChanges(filterId).send();

        List<AppLog.LogResult> logResults = appLog.getLogs();


        System.out.println(logResults.size());
        for (AppLog.LogResult logResult : logResults) {
            Object s = logResult.get();
            System.out.println("Block Hash" + s.toString());
            AppBlock block = service.appGetBlockByHash(s.toString(), true).send();
            System.out.println("Block number: " + block.getBlock().getHeader().getNumber() + "\n");
        }
    }
}
