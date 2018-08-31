package org.nervos.appchain.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.protocol.core.methods.response.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppLog;
import org.nervos.appchain.protocol.http.HttpService;

public class BlockFilterTransactionExample {
    private static Properties props;
    private static String testNetIpAddr;
    private static Nervosj service;

    static {
        props = Config.load();
        testNetIpAddr = props.getProperty(Config.TEST_NET_ADDR);

        HttpService.setDebug(false);
        service = Nervosj.build(new HttpService(testNetIpAddr));
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
