package org.nervos.appchain.protocol.core;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.nervos.appchain.protocol.ResponseTester;
import org.nervos.appchain.protocol.core.methods.response.AppAccounts;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.protocol.core.methods.response.AppBlockNumber;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.core.methods.response.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppGetBalance;
import org.nervos.appchain.protocol.core.methods.response.AppGetCode;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionCount;
import org.nervos.appchain.protocol.core.methods.response.AppGetTransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.AppLog;
import org.nervos.appchain.protocol.core.methods.response.AppSendRawTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppSign;
import org.nervos.appchain.protocol.core.methods.response.AppTransaction;
import org.nervos.appchain.protocol.core.methods.response.AppUninstallFilter;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.NetPeerCount;
import org.nervos.appchain.protocol.core.methods.response.Transaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.core.methods.response.Web3ClientVersion;
import org.nervos.appchain.protocol.core.methods.response.Web3Sha3;

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
    public void testWeb3ClientVersion() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"Mist/v0.9.3/darwin/go1.4.1\"\n"
                        + "}"
        );

        Web3ClientVersion web3ClientVersion = deserialiseResponse(Web3ClientVersion.class);
        assertThat(web3ClientVersion.getWeb3ClientVersion(), is("Mist/v0.9.3/darwin/go1.4.1"));
    }

    @Test
    public void testWeb3Sha3() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":64,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad\"\n"
                        + "}"
        );

        Web3Sha3 web3Sha3 = deserialiseResponse(Web3Sha3.class);
        assertThat(web3Sha3.getResult(),
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

    @Test
    public void testEthBlockTransactionHashes() {
        //CHECKSTYLE:OFF
        buildResponse(
            "{\n"
                    + "\"jsonrpc\":\"2.0\",\n"
                    + "\"id\":1,\n"
                    + "\"result\":{\n"
                    + " \"version\":0,"
                    + " \"hash\":\"0xda9e8497221e9d18131292f8b459d62e03c882be4666d084c67b8dcebcce91d1\",\n"
                    + " \"header\":{\n"
                    + "     \"timestamp\":1533101297835,\n"
                    + "     \"prevHash\":\"0x4391078f4c03c028178e8f1a9f25392a634b098d7737601f1679a8b197fafcf9\","
                    + "     \"number\":\"0x3ff59\","
                    + "     \"stateRoot\":\"0x60fb67e5ca3868d292ea44e2c28bb16d83d25793ed629b28f4c8d18de8742a72\","
                    + "     \"transactionsRoot\":\"0x62d54aa5b82a5813c87a3a4d1d2c3a02e6c88f037c8fb2461e5116e7a9dc2149\","
                    + "     \"receiptsRoot\":\"0x918334ef71e85bed370065d1f2758d1eb3f089f7e5323a78a633de7a5f07f371\","
                    + "     \"gasUsed\":\"0x132bd\","
                    + "     \"proof\":"
                    + "         {\"Bft\":{"
                    + "             \"proposal\":\"0xe1b9bba13cb64a920c04f3abc2ea0a98d2db4fb65d233df3afc31c5321bb6054\","
                    + "             \"height\":261976,"
                    + "             \"round\":0,"
                    + "             \"commits\":{"
                    + "                 \"0x486bb688c8d29056bd7f87c26733048b0a6abda6\":\"0x0fc60edaff5f00329e088750119e92af8940e9612d1dbf3ce4158e721faaff592ab43f4bc3c24718631aa4140f8412cefb9086416ba2c280fa85ec83b511ed5f00\","
                    + "                 \"0x31042d4f7662cddf8ded5229db3c5e7302875e10\":\"0x68e3701cec53f96e792ecb02eb390dad074eb26b0d5472be8804929e016cc99d647b9c40e30942270f1fed91f3fc14479c47de603a5de99735b3122b5d08e2c601\","
                    + "                 \"0xee01b9ba97671e8a1891e85b206b499f106822a1\":\"0x34272376d15b8e0658efa8cce056f59d5054b5863a9945c5e8e232c65f2d434476833899912a871e58374c08da78e8279dc02c877221f6a880134ed050bae44700\","
                    + "                 \"0x71b028e49c6f41aaa74932d703c707ecca6d732e\":\"0x7ed41b1ccf137a77eade8bbd4e348534dc26c45c7d8a547adc7286746497f6bf3587f886c87e97dbfadd8e2698827c53123a3fbfddb682c887509342ae030dec01\""
                    + "                 }"
                    + "             }"
                    + "         },"
                    + " \"proposer\":\"0xee01b9ba97671e8a1891e85b206b499f106822a1\""
                    + " },"
                    + " \"body\":{"
                    + "     \"transactions\":["
                    + "             {"
                    + "                 \"hash\":\"0xab64a7be5f38ab8061419472402c52c9a26f5989b26da5eec4d59d7aa68348e1\","
                    + "                 \"content\":\"0x0aad010a2839663864396337633336616632336561656230323032363938333135656163323334653334373039120f65333031643366313762346566393518c0843d20a6ff0f2a44a9059cbb000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa1800000000000000000000000000000000000000000000000000000000000003e83220000000000000000000000000000000000000000000000000000000000000000038011241e1e76fe6f033db9527f0714ba81b4af1f26b9d70210acbf3cebcb916c2f4c7ae70c3fe80a2032d748caa0b90b70159263880ea6b230912f5bb5c62b234ed67a401\""
                    + "             },"
                    + "             {"
                    + "                 \"hash\":\"0xcd7fc94a452d78b0041abac36de489c19432d3c208e795099ac13a6327bb4bd8\","
                    + "                 \"content\":\"0x0aad010a2839663864396337633336616632336561656230323032363938333135656163323334653334373039120f65316239383236653737326132306518c0843d20a7ff0f2a44a9059cbb000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa1800000000000000000000000000000000000000000000000000000000000003e83220000000000000000000000000000000000000000000000000000000000000000038011241d5c91c9e262c57fd79d6979f8b9205d7d49ed8918edaf8eb8b8e3bf5447b7d6745e57ebcb741486064758c0058750569c4e6a759de293563e9fa156172abcc3500\""
                    + "             }"
                    + "         ]"
                    + "     }"
                    + " }"
                    + "}");


        AppBlock.TendermintCommit[] tendermintCommits = {
                new AppBlock.TendermintCommit(
                        "0x486bb688c8d29056bd7f87c26733048b0a6abda6",
                        "0x0fc60edaff5f00329e088750119e92af8940e9612d1dbf3ce4158e721faaff592ab43f4bc3c24718631aa4140f8412cefb9086416ba2c280fa85ec83b511ed5f00"),
                new AppBlock.TendermintCommit(
                        "0x31042d4f7662cddf8ded5229db3c5e7302875e10",
                        "0x68e3701cec53f96e792ecb02eb390dad074eb26b0d5472be8804929e016cc99d647b9c40e30942270f1fed91f3fc14479c47de603a5de99735b3122b5d08e2c601"),
                new AppBlock.TendermintCommit(
                        "0xee01b9ba97671e8a1891e85b206b499f106822a1",
                        "0x34272376d15b8e0658efa8cce056f59d5054b5863a9945c5e8e232c65f2d434476833899912a871e58374c08da78e8279dc02c877221f6a880134ed050bae44700"),
                new AppBlock.TendermintCommit(
                        "0x71b028e49c6f41aaa74932d703c707ecca6d732e",
                        "0x7ed41b1ccf137a77eade8bbd4e348534dc26c45c7d8a547adc7286746497f6bf3587f886c87e97dbfadd8e2698827c53123a3fbfddb682c887509342ae030dec01"),
        };
        AppBlock.Tendermint tendermint = new AppBlock.Tendermint(
                "0xe1b9bba13cb64a920c04f3abc2ea0a98d2db4fb65d233df3afc31c5321bb6054",
                "261976", "0",tendermintCommits);

        AppBlock.Header header = new AppBlock.Header(
                1533101297835L,
                "0x4391078f4c03c028178e8f1a9f25392a634b098d7737601f1679a8b197fafcf9",
                "0x3ff59",
                "0x60fb67e5ca3868d292ea44e2c28bb16d83d25793ed629b28f4c8d18de8742a72",
                "0x62d54aa5b82a5813c87a3a4d1d2c3a02e6c88f037c8fb2461e5116e7a9dc2149",
                "0x918334ef71e85bed370065d1f2758d1eb3f089f7e5323a78a633de7a5f07f371",
                "0x132bd",
                new AppBlock.Proof(tendermint)
        );

        List<AppBlock.TransactionObject> transactionObjects = new ArrayList<>();
        AppBlock.TransactionObject txObj1 = new AppBlock.TransactionObject();
        txObj1.setHash("0xab64a7be5f38ab8061419472402c52c9a26f5989b26da5eec4d59d7aa68348e1");
        txObj1.setContent("0x0aad010a2839663864396337633336616632336561656230323032363938333135656163323334653334373039120f65333031643366313762346566393518c0843d20a6ff0f2a44a9059cbb000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa1800000000000000000000000000000000000000000000000000000000000003e83220000000000000000000000000000000000000000000000000000000000000000038011241e1e76fe6f033db9527f0714ba81b4af1f26b9d70210acbf3cebcb916c2f4c7ae70c3fe80a2032d748caa0b90b70159263880ea6b230912f5bb5c62b234ed67a401");

        AppBlock.TransactionObject txObj2 = new AppBlock.TransactionObject();
        txObj2.setHash("0xcd7fc94a452d78b0041abac36de489c19432d3c208e795099ac13a6327bb4bd8");
        txObj2.setContent("0x0aad010a2839663864396337633336616632336561656230323032363938333135656163323334653334373039120f65316239383236653737326132306518c0843d20a7ff0f2a44a9059cbb000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa1800000000000000000000000000000000000000000000000000000000000003e83220000000000000000000000000000000000000000000000000000000000000000038011241d5c91c9e262c57fd79d6979f8b9205d7d49ed8918edaf8eb8b8e3bf5447b7d6745e57ebcb741486064758c0058750569c4e6a759de293563e9fa156172abcc3500");

        transactionObjects.add(txObj1);
        transactionObjects.add(txObj2);
        AppBlock.Body body = new AppBlock.Body(transactionObjects);

        String version = "0";
        String hash = "0xda9e8497221e9d18131292f8b459d62e03c882be4666d084c67b8dcebcce91d1";
        AppBlock.Block block = new AppBlock.Block(
                version,
                hash,
                header,
                body
        );
        //CHECKSTYLE:ON
        AppBlock appBlock = deserialiseResponse(AppBlock.class);
        assertThat(appBlock.getBlock(), equalTo(block));
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

    // Remove once Geth & Parity return the same v value in transactions
    @Ignore //no support for geth
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
                        "       \"index\":\"0x1\"\n" +
                        "   }\n" +
                        "}");

        Transaction transaction = new Transaction(
                "0xcd7fc94a452d78b0041abac36de489c19432d3c208e795099ac13a6327bb4bd8",
                "0xda9e8497221e9d18131292f8b459d62e03c882be4666d084c67b8dcebcce91d1",
                "0x3ff59",
                "0x0aad010a2839663864396337633336616632336561656230323032363938333135656163323334653334373039120f65316239383236653737326132306518c0843d20a7ff0f2a44a9059cbb000000000000000000000000bac68e5cb986ead0253e0632da1131a0a96efa1800000000000000000000000000000000000000000000000000000000000003e83220000000000000000000000000000000000000000000000000000000000000000038011241d5c91c9e262c57fd79d6979f8b9205d7d49ed8918edaf8eb8b8e3bf5447b7d6745e57ebcb741486064758c0058750569c4e6a759de293563e9fa156172abcc3500",
                "0x1"
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
                        + "        \"cumulativeGasUsed\": \"0x33bc\",\n"
                        + "        \"gasUsed\": \"0x4dc\",\n"
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
                        "0x33bc",
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
                        + "        \"cumulativeGasUsed\": \"0x33bc\",\n"
                        + "        \"gasUsed\": \"0x4dc\",\n"
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
                        "0x33bc",
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

        List<AppLog.LogResult> logs = Collections.<AppLog.LogResult>singletonList(
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
