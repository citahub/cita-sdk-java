package com.citahub.cita.protocol.core;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.citahub.cita.protocol.ResponseTester;
import com.citahub.cita.protocol.core.methods.response.AppAccounts;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.AppBlockNumber;
import com.citahub.cita.protocol.core.methods.response.AppCall;
import com.citahub.cita.protocol.core.methods.response.AppFilter;
import com.citahub.cita.protocol.core.methods.response.AppGetBalance;
import com.citahub.cita.protocol.core.methods.response.AppGetCode;
import com.citahub.cita.protocol.core.methods.response.AppGetTransactionCount;
import com.citahub.cita.protocol.core.methods.response.AppGetTransactionReceipt;
import com.citahub.cita.protocol.core.methods.response.AppLog;
import com.citahub.cita.protocol.core.methods.response.AppSendRawTransaction;
import com.citahub.cita.protocol.core.methods.response.AppSendTransaction;
import com.citahub.cita.protocol.core.methods.response.AppSign;
import com.citahub.cita.protocol.core.methods.response.AppTransaction;
import com.citahub.cita.protocol.core.methods.response.AppUninstallFilter;
import com.citahub.cita.protocol.core.methods.response.Log;
import com.citahub.cita.protocol.core.methods.response.NetPeerCount;
import com.citahub.cita.protocol.core.methods.response.Transaction;
import com.citahub.cita.protocol.core.methods.response.TransactionReceipt;
import com.citahub.cita.protocol.core.methods.response.CITAClientVersion;
import com.citahub.cita.protocol.core.methods.response.CITASha3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Core Protocol Response tests.
 */
public class ResponseTest extends ResponseTester {

    @Test
    public void testErrorResponse() {
        buildResponse(
                "{"
                        + "  \"jsonrpc\":\"2.0\","
                        + "  \"id\":1,"
                        + "  \"error\":{"
                        + "    \"code\":-32602,"
                        + "    \"message\":\"Invalid address length, expected 40 got 64 bytes\","
                        + "    \"data\":null"
                        + "  }"
                        + "}"
        );

        AppBlock appBlock = deserialiseResponse(AppBlock.class);
        assertTrue(appBlock.hasError());
        assertThat(appBlock.getError(), equalTo(
                new Response.Error(-32602, "Invalid address length, expected 40 got 64 bytes")));
    }

    @Test
    public void testCitaClientVersion() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"Mist/v0.9.3/darwin/go1.4.1\"\n"
                        + "}"
        );

        CITAClientVersion citaClientVersion = deserialiseResponse(CITAClientVersion.class);
        assertThat(citaClientVersion.getCitaClientVersion(), is("Mist/v0.9.3/darwin/go1.4.1"));
    }

    @Test
    public void testCITASha3() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":64,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad\"\n"
                        + "}"
        );

        CITASha3 CITASha3 = deserialiseResponse(CITASha3.class);
        assertThat(CITASha3.getResult(),
                is("0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"));
    }

    @Test
    public void testNetPeerCount() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":74,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x2\"\n"
                        + "}"
        );

        NetPeerCount netPeerCount = deserialiseResponse(NetPeerCount.class);
        assertThat(netPeerCount.getQuantity(), equalTo(BigInteger.valueOf(2L)));
    }


    @Test
    public void testEthAccounts() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": [\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"]\n"
                        + "}"
        );

        AppAccounts appAccounts = deserialiseResponse(AppAccounts.class);
        assertThat(appAccounts.getAccounts(),
                equalTo(Arrays.asList("0x407d73d8a49eeb85d32cf465507dd71d507100c1")));
    }

    @Test
    public void testEthBlockNumber() {
        buildResponse(
                "{\n"
                        + "  \"id\":83,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x4b7\"\n"
                        + "}"
        );

        AppBlockNumber appBlockNumber = deserialiseResponse(AppBlockNumber.class);
        assertThat(appBlockNumber.getBlockNumber(), equalTo(BigInteger.valueOf(1207L)));
    }

    @Test
    public void testEthGetBalance() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x234c8a3397aab58\"\n"
                        + "}"
        );

        AppGetBalance appGetBalance = deserialiseResponse(AppGetBalance.class);
        assertThat(appGetBalance.getBalance(), equalTo(BigInteger.valueOf(158972490234375000L)));
    }

    @Test
    public void testEthGetTransactionCount() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}"
        );

        AppGetTransactionCount appGetTransactionCount =
                deserialiseResponse((AppGetTransactionCount.class));
        assertThat(appGetTransactionCount.getTransactionCount(), equalTo(BigInteger.valueOf(1L)));
    }

    @Test
    public void testGetCode() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x600160008035811a818181146012578301005b601b60013560255"
                        + "65b8060005260206000f25b600060078202905091905056\"\n"
                        + "}"
        );

        AppGetCode appGetCode = deserialiseResponse(AppGetCode.class);
        assertThat(appGetCode.getCode(),
                is("0x600160008035811a818181146012578301005b601b60013560255"
                        + "65b8060005260206000f25b600060078202905091905056"));
    }

    @Test
    public void testEthSign() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b712"
                        + "7ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001\"\n"
                        + "}"
        );

        AppSign appSign = deserialiseResponse(AppSign.class);
        assertThat(appSign.getSignature(),
                is("0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e9"
                        + "96cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001"));
    }

    @Ignore //sendTransaction is not supported by CITA
    @Test
    public void testEthSendTransaction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\"\n"
                        + "}"
        );

        AppSendTransaction appSendTransaction = deserialiseResponse(AppSendTransaction.class);
        assertThat(appSendTransaction.getSendTransactionResult().getHash(),
                is("0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"));
    }

    @Test
    public void testEthSendRawTransaction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\"\n"
                        + "}"
        );

        AppSendRawTransaction appSendRawTransaction =
                deserialiseResponse(AppSendRawTransaction.class);
        assertThat(appSendRawTransaction.getTransactionHash(),
                is("0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"));
    }

    @Test
    public void testEthCall() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x\"\n"
                        + "}"
        );

        AppCall appCall = deserialiseResponse(AppCall.class);
        assertThat(appCall.getValue(), is("0x"));
    }


    @Ignore //no support for parity
    @Test
    public void testEthBlockFullTransactionsParity() {
        //CHECKSTYLE:OFF
        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"number\": \"0x1b4\",\n"
                        + "    \"hash\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"parentHash\": \"0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5\",\n"
                        + "    \"nonce\": \"0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2\",\n"
                        + "    \"sha3Uncles\": \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "    \"logsBloom\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"transactionsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"stateRoot\": \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\",\n"
                        + "    \"receiptsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"author\": \"0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6\",\n"
                        + "    \"miner\": \"0x4e65fda2159562a496f9f3522f89122a3088497a\",\n"
                        + "    \"mixHash\": \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "    \"difficulty\": \"0x027f07\",\n"
                        + "    \"totalDifficulty\":  \"0x027f07\",\n"
                        + "    \"extraData\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "    \"size\":  \"0x027f07\",\n"
                        + "    \"gasLimit\": \"0x9f759\",\n"
                        + "    \"gasUsed\": \"0x9f759\",\n"
                        + "    \"timestamp\": \"0x54e34e8e\",\n"
                        + "    \"transactions\": [{"
                        + "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"nonce\":\"0x\",\n"
                        + "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0x15df\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"value\":\"0x7f110\",\n"
                        + "        \"gas\": \"0x7f110\",\n"
                        + "        \"gasPrice\":\"0x09184e72a000\",\n"
                        + "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\","
                        + "        \"creates\":null,\n"
                        + "        \"publicKey\":\"0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b\",\n"
                        + "        \"raw\":\"0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"r\":\"0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc\",\n"
                        + "        \"s\":\"0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"v\":0\n"
                        + "    }], \n"
                        + "    \"uncles\": [\n"
                        + "       \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "       \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\"\n"
                        + "    ],\n"
                        + "    \"sealFields\": [\n"
                        + "       \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "       \"0x39a3eb432fbef1fc\"\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}"
        );
        //CHECKSTYLE:ON
        AppBlock appBlock = deserialiseResponse(AppBlock.class);
        AppBlock.Header header = new AppBlock.Header();
        AppBlock.Body body = new AppBlock.Body();
        String version = "0";
        String hash = "";
        AppBlock.Block block = new AppBlock.Block(version, hash, header, body);
        assertThat(appBlock.getBlock(), equalTo(block));
    }

    @Ignore
    @Test
    public void testEthBlockFullTransactionsGeth() {
        //CHECKSTYLE:OFF
        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"number\": \"0x1b4\",\n"
                        + "    \"hash\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"parentHash\": \"0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5\",\n"
                        + "    \"nonce\": \"0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2\",\n"
                        + "    \"sha3Uncles\": \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "    \"logsBloom\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"transactionsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"stateRoot\": \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\",\n"
                        + "    \"receiptsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"author\": \"0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6\",\n"
                        + "    \"miner\": \"0x4e65fda2159562a496f9f3522f89122a3088497a\",\n"
                        + "    \"mixHash\": \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "    \"difficulty\": \"0x027f07\",\n"
                        + "    \"totalDifficulty\":  \"0x027f07\",\n"
                        + "    \"extraData\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "    \"size\":  \"0x027f07\",\n"
                        + "    \"gasLimit\": \"0x9f759\",\n"
                        + "    \"gasUsed\": \"0x9f759\",\n"
                        + "    \"timestamp\": \"0x54e34e8e\",\n"
                        + "    \"transactions\": [{"
                        + "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"nonce\":\"0x\",\n"
                        + "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0x15df\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"value\":\"0x7f110\",\n"
                        + "        \"gas\": \"0x7f110\",\n"
                        + "        \"gasPrice\":\"0x09184e72a000\",\n"
                        + "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\","
                        + "        \"creates\":null,\n"
                        + "        \"publicKey\":\"0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b\",\n"
                        + "        \"raw\":\"0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"r\":\"0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc\",\n"
                        + "        \"s\":\"0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"v\":\"0x9d\"\n"
                        + "    }], \n"
                        + "    \"uncles\": [\n"
                        + "       \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "       \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\"\n"
                        + "    ],\n"
                        + "    \"sealFields\": [\n"
                        + "       \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "       \"0x39a3eb432fbef1fc\"\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}"
        );
        //CHECKSTYLE:ON

        AppBlock appBlock = deserialiseResponse(AppBlock.class);
        AppBlock.Header header = new AppBlock.Header();
        AppBlock.Body body = new AppBlock.Body();
        String version = "0";
        String hash = "";
        AppBlock.Block block = new AppBlock.Block(version, hash, header, body);
        assertThat(appBlock.getBlock(), equalTo(block));
    }

    @Test
    public void testEthBlockNull() {
        buildResponse(
                "{\n"
                        + "  \"result\": null\n"
                        + "}"
        );

        AppBlock appBlock = deserialiseResponse(AppBlock.class);
        assertNull(appBlock.getBlock());
    }

    @Test
    public void testEthTransaction() {
        //CHECKSTYLE:OFF
        buildResponse(
                "{\n" +
                        "   \"jsonrpc\":\"2.0\",\n" +
                        "   \"id\":1,\n" +
                        "   \"result\":{\n" +
                        "       \"hash\":\"0xcd7fc94a452d78b0041abac36de489c19432d3c208e795099ac13a6327bb4bd8\",\n" +
                        "       \"content\":\"0x0aad010a2839663864396337633336616632336561656230323032363938333135656163323334653334373039120f65316239383236653737326132306518c0843d20a7ff0f2a44a9059cbb000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa1800000000000000000000000000000000000000000000000000000000000003e83220000000000000000000000000000000000000000000000000000000000000000038011241d5c91c9e262c57fd79d6979f8b9205d7d49ed8918edaf8eb8b8e3bf5447b7d6745e57ebcb741486064758c0058750569c4e6a759de293563e9fa156172abcc3500\",\n" +
                        "       \"blockNumber\":\"0x3ff59\",\n" +
                        "       \"blockHash\":\"0xda9e8497221e9d18131292f8b459d62e03c882be4666d084c67b8dcebcce91d1\",\n" +
                        "       \"index\":\"0x1\",\n" +
                        "       \"from\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\"\n" +
                        "   }\n" +
                        "}");

        Transaction transaction = new Transaction(
                "0xcd7fc94a452d78b0041abac36de489c19432d3c208e795099ac13a6327bb4bd8",
                "0xda9e8497221e9d18131292f8b459d62e03c882be4666d084c67b8dcebcce91d1",
                "0x3ff59",
                "0x0aad010a2839663864396337633336616632336561656230323032363938333135656163323334653334373039120f65316239383236653737326132306518c0843d20a7ff0f2a44a9059cbb000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa1800000000000000000000000000000000000000000000000000000000000003e83220000000000000000000000000000000000000000000000000000000000000000038011241d5c91c9e262c57fd79d6979f8b9205d7d49ed8918edaf8eb8b8e3bf5447b7d6745e57ebcb741486064758c0058750569c4e6a759de293563e9fa156172abcc3500",
                "0x1",
                "0x85h43d8a49eeb85d32cf465507dd71d507100c1"
        );
        //CHECKSTYLE:ON

        AppTransaction appTransaction = deserialiseResponse(AppTransaction.class);
        assertThat(appTransaction.getTransaction(), equalTo(transaction));
    }

    @Test
    public void testEthTransactionNull() {
        buildResponse(
                "{\n"
                        + "  \"result\": null\n"
                        + "}"
        );

        AppTransaction appTransaction = deserialiseResponse(AppTransaction.class);
        assertNull(appTransaction.getTransaction());
    }

    @Test
    public void testeAppGetTransactionReceiptBeforeByzantium() {
        //CHECKSTYLE:OFF
        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": {\n"
                        + "        \"transactionHash\": \"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"blockHash\": \"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0xb\",\n"
                        + "        \"cumulativeGasUsed\": \"\",\n"
                        + "        \"cumulativeQuotaUsed\": \"0x33bc\",\n"
                        + "        \"gasUsed\": \"\",\n"
                        + "        \"quotaUsed\": \"0x4dc\",\n"
                        + "        \"contractAddress\": \"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\n"
                        + "        \"root\": \"9307ba10e41ecf3d40507fc908655fe72fc129d46f6d99baf7605d0e29184911\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"logs\": [{\n"
                        + "            \"removed\": false,\n"
                        + "            \"logIndex\": \"0x1\",\n"
                        + "            \"transactionIndex\": \"0x0\",\n"
                        + "            \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n"
                        + "            \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"blockNumber\":\"0x1b4\",\n"
                        + "            \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "            \"transactionLogIndex\":\"mined\",\n"
                        + "            \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]"
                        + "        }],\n"
                        + "        \"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "        \"errorMessage\":\"errorMessage\"\n"
                        + "  }\n"
                        + "}"
        );

        TransactionReceipt transactionReceipt =
                new TransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238",
                        "0x1",
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0xb",
                        "",
                        "0x33bc",
                        "",
                        "0x4dc",
                        "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        "9307ba10e41ecf3d40507fc908655fe72fc129d46f6d99baf7605d0e29184911",
                        null,
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                        Arrays.asList(
                                new Log(
                                        false,
                                        "0x1",
                                        "0x0",
                                        "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                                        "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x1b4",
                                        "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                                        "mined",
                                        Arrays.asList(
                                                "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5"
                                        )
                                )
                        ),
                        "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                        "errorMessage"

                );
        //CHECKSTYLE:ON

        AppGetTransactionReceipt appGetTransactionReceipt = deserialiseResponse(
                AppGetTransactionReceipt.class);
        assertThat(appGetTransactionReceipt.getTransactionReceipt(),
                equalTo(transactionReceipt));
    }

    @Test
    public void testeAppGetTransactionReceiptAfterByzantium() {
        //CHECKSTYLE:OFF
        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": {\n"
                        + "        \"transactionHash\": \"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"blockHash\": \"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0xb\",\n"
                        + "        \"cumulativeGasUsed\": \"\",\n"
                        + "        \"cumulativeQuotaUsed\": \"0x33bc\",\n"
                        + "        \"gasUsed\": \"\",\n"
                        + "        \"quotaUsed\": \"0x4dc\",\n"
                        + "        \"contractAddress\": \"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\n"
                        + "        \"status\": \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"logs\": [{\n"
                        + "            \"removed\": false,\n"
                        + "            \"logIndex\": \"0x1\",\n"
                        + "            \"transactionIndex\": \"0x0\",\n"
                        + "            \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n"
                        + "            \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"blockNumber\":\"0x1b4\",\n"
                        + "            \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "            \"transactionLogIndex\":\"mined\",\n"
                        + "            \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]"
                        + "        }],\n"
                        + "        \"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "        \"errorMessage\":\"errorMessage\"\n"
                        + "  }\n"
                        + "}"
        );

        TransactionReceipt transactionReceipt =
                new TransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238",
                        "0x1",
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0xb",
                        "",
                        "0x33bc",
                        "",
                        "0x4dc",
                        "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        null,
                        "0x1",
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                        Arrays.asList(
                                new Log(
                                        false,
                                        "0x1",
                                        "0x0",
                                        "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                                        "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x1b4",
                                        "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                                        "mined",
                                        Arrays.asList(
                                                "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5"
                                        )
                                )
                        ),
                        "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                        "errorMessage"
                );
        //CHECKSTYLE:ON

        AppGetTransactionReceipt appGetTransactionReceipt = deserialiseResponse(
                AppGetTransactionReceipt.class);
        assertThat(appGetTransactionReceipt.getTransactionReceipt(),
                equalTo(transactionReceipt));
    }

    @Test
    public void testEthFilter() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}"
        );

        AppFilter appFilter = deserialiseResponse(AppFilter.class);
        assertThat(appFilter.getFilterId(), is(BigInteger.valueOf(1)));
    }

    @Test
    public void testAppUninstallFilter() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}"
        );

        AppUninstallFilter appUninstallFilter = deserialiseResponse(AppUninstallFilter.class);
        assertThat(appUninstallFilter.isUninstalled(), is(true));
    }

    @Test
    public void testEthLog() {
        //CHECKSTYLE:OFF
        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": [{\n"
                        + "        \"removed\": false,\n"
                        + "        \"logIndex\": \"0x1\",\n"
                        + "        \"transactionIndex\": \"0x0\",\n"
                        + "        \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n"
                        + "        \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "        \"blockNumber\":\"0x1b4\",\n"
                        + "        \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "        \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "        \"transactionLogIndex\":\"mined\",\n"
                        + "        \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]"
                        + "    }]"
                        + "}"
        );
        //CHECKSTYLE:ON

        List<Log> logs = Collections.singletonList(
                new AppLog.LogObject(
                        false,
                        "0x1",
                        "0x0",
                        "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                        "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                        "0x1b4",
                        "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                        "mined",
                        Collections.singletonList(
                                "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5"
                        )
                )
        );

        AppLog appLog = deserialiseResponse(AppLog.class);
        assertThat(appLog.getLogs(), is(logs));
    }
}
