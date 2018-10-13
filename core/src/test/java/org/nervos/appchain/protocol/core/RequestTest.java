package org.nervos.appchain.protocol.core;

import org.junit.Test;

import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.RequestTester;
import org.nervos.appchain.protocol.core.methods.request.AppFilter;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.http.HttpService;
import org.nervos.appchain.utils.Numeric;

public class RequestTest extends RequestTester {

    private AppChainj appChainj;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        appChainj = AppChainj.build(httpService);
    }

    @Test
    public void testNetPeerCount() throws Exception {
        appChainj.netPeerCount().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"peerCount\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testAppAccounts() throws Exception {
        appChainj.appAccounts().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"accounts\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testAppBlockNumber() throws Exception {
        appChainj.appBlockNumber().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"blockNumber\",\"params\":[],\"id\":1}");
    }

    @Test
    public void testAppGetBalance() throws Exception {
        appChainj.appGetBalance("0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                DefaultBlockParameterName.LATEST).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getBalance\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"latest\"],"
                        + "\"id\":1}");
    }


    @Test
    public void testAppGetTransactionCount() throws Exception {
        appChainj.appGetTransactionCount("0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                DefaultBlockParameterName.LATEST).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getTransactionCount\","
                + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"latest\"],"
                + "\"id\":1}");
    }

    @Test
    public void testAppGetCode() throws Exception {
        appChainj.appGetCode("0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b",
                DefaultBlockParameter.valueOf(Numeric.toBigInt("0x2"))).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getCode\","
                + "\"params\":[\"0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b\",\"0x2\"],\"id\":1}");
    }

    @Test
    public void testAppSign() throws Exception {
        appChainj.appSign("0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab",
                "0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470").send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"sign\","
                + "\"params\":[\"0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab\","
                + "\"0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470\"],"
                + "\"id\":1}");
    }

    @Test
    public void testAppSendRawTransaction() throws Exception {
        appChainj.appSendRawTransaction(
                "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f"
                        + "072445675058bb8eb970870f072445675").send();

        //CHECKSTYLE:OFF
        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"sendRawTransaction\",\"params\":[\"0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675\"],\"id\":1}");
        //CHECKSTYLE:ON
    }


    @Test
    public void testAppCall() throws Exception {
        appChainj.appCall(new Call(
                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        "0x0"),
                DefaultBlockParameter.valueOf("latest")).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"call\","
                + "\"params\":[{\"from\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\","
                + "\"to\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\"data\":\"0x0\"},"
                + "\"latest\"],\"id\":1}");
    }


    @Test
    public void testAppGetBlockByHash() throws Exception {
        appChainj.appGetBlockByHash(
                "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331", true).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getBlockByHash\",\"params\":["
                        + "\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\""
                        + ",true],\"id\":1}");
    }

    @Test
    public void testAppGetBlockByNumber() throws Exception {
        appChainj.appGetBlockByNumber(
                DefaultBlockParameter.valueOf(Numeric.toBigInt("0x1b4")), true).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getBlockByNumber\","
                + "\"params\":[\"0x1b4\",true],\"id\":1}");
    }

    @Test
    public void testAppGetTransactionByHash() throws Exception {
        appChainj.appGetTransactionByHash(
                "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238").send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getTransaction\",\"params\":["
                + "\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],"
                + "\"id\":1}");
    }

    @Test
    public void testAppGetTransactionReceipt() throws Exception {
        appChainj.appGetTransactionReceipt(
                "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238").send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getTransactionReceipt\",\"params\":["
                + "\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],"
                + "\"id\":1}");
    }

    @Test
    public void testAppNewFilter() throws Exception {
        AppFilter appFilter = new AppFilter()
                .addSingleTopic("0x12341234");

        appChainj.appNewFilter(appFilter).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"newFilter\","
                + "\"params\":[{\"topics\":[\"0x12341234\"]}],\"id\":1}");
    }

    @Test
    public void testAppNewBlockFilter() throws Exception {
        appChainj.appNewBlockFilter().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"newBlockFilter\","
                + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testAppNewPendingTransactionFilter() throws Exception {
        appChainj.appNewPendingTransactionFilter().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"newPendingTransactionFilter\","
                + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testAppUninstallFilter() throws Exception {
        appChainj.appUninstallFilter(Numeric.toBigInt("0xb")).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"uninstallFilter\","
                + "\"params\":[\"0xb\"],\"id\":1}");
    }

    @Test
    public void testAppGetFilterChanges() throws Exception {
        appChainj.appGetFilterChanges(Numeric.toBigInt("0x16")).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getFilterChanges\","
                + "\"params\":[\"0x16\"],\"id\":1}");
    }

    @Test
    public void testAppGetFilterLogs() throws Exception {
        appChainj.appGetFilterLogs(Numeric.toBigInt("0x16")).send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getFilterLogs\","
                + "\"params\":[\"0x16\"],\"id\":1}");
    }

    @Test
    public void testAppGetLogs() throws Exception {
        appChainj.appGetLogs(new AppFilter().addSingleTopic(
                "0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b"))
                .send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"getLogs\","
                + "\"params\":[{\"topics\":["
                + "\"0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b\"]}],"
                + "\"id\":1}");
    }

    @Test
    public void testAppGetLogsWithNumericBlockRange() throws Exception {
        appChainj.appGetLogs(new AppFilter(
                DefaultBlockParameter.valueOf(Numeric.toBigInt("0xe8")),
                DefaultBlockParameter.valueOf("latest"), ""))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"getLogs\","
                        + "\"params\":[{\"topics\":[],\"fromBlock\":\"0xe8\","
                        + "\"toBlock\":\"latest\",\"address\":[\"\"]}],\"id\":1}");
    }
}
