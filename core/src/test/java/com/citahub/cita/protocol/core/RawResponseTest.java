package com.citahub.cita.protocol.core;

import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.CITAClientVersion;
import org.junit.Test;

import com.citahub.cita.protocol.ResponseTester;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Raw Response tests.
 */
public class RawResponseTest extends ResponseTester {

    private static final String RAW_RESPONSE = "{\n"
            + "  \"id\":67,\n"
            + "  \"jsonrpc\":\"2.0\",\n"
            + "  \"result\": \"Mist/v0.9.3/darwin/go1.4.1\"\n"
            + "}";

    //CHECKSTYLE:OFF
    private static final String LARGE_RAW_RESPONSE = "{\n"
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
            + "}";
    //CHECKSTYLE:ON

    @Test
    public void testRawResponseEnabled() {
        configureCITAService(true);
        final CITAClientVersion citaClientVersion = deserialiseCitaClientVersionResponse();
        assertThat(citaClientVersion.getRawResponse(), is(RAW_RESPONSE));
    }

    @Test
    public void testLargeRawResponseEnabled() {
        configureCITAService(true);

        buildResponse(LARGE_RAW_RESPONSE);

        AppBlock ethBlock = deserialiseResponse(AppBlock.class);
        assertThat(ethBlock.getRawResponse(), is(LARGE_RAW_RESPONSE));
    }

    @Test
    public void testRawResponseDisabled() {
        configureCITAService(false);
        final CITAClientVersion citaClientVersion = deserialiseCitaClientVersionResponse();
        assertThat(citaClientVersion.getRawResponse(), nullValue());
    }

    private CITAClientVersion deserialiseCitaClientVersionResponse() {
        buildResponse(RAW_RESPONSE);

        return deserialiseResponse(CITAClientVersion.class);
    }
}
