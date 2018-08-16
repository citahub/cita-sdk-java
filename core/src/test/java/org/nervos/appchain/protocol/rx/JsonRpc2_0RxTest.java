package org.nervos.appchain.protocol.rx;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjFactory;
import org.nervos.appchain.protocol.NervosjService;
import org.nervos.appchain.protocol.ObjectMapperFactory;
import org.nervos.appchain.protocol.core.DefaultBlockParameterNumber;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.protocol.core.methods.response.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppLog;
import org.nervos.appchain.protocol.core.methods.response.AppUninstallFilter;
import org.nervos.appchain.utils.Numeric;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonRpc2_0RxTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private Nervosj nervosj;

    private NervosjService nervosjService;

    @Before
    public void setUp() {
        nervosjService = mock(NervosjService.class);
        nervosj = NervosjFactory.build(nervosjService, 1000, Executors.newSingleThreadScheduledExecutor());
    }

    @Test
    public void testReplayBlocksObservable() throws Exception {

        List<AppBlock> appBlocks = Arrays.asList(createBlock(0), createBlock(1), createBlock(2));

        OngoingStubbing<AppBlock> stubbing =
                when(nervosjService.send(any(Request.class), eq(AppBlock.class)));
        for (AppBlock appBlock : appBlocks) {
            stubbing = stubbing.thenReturn(appBlock);
        }

        Observable<AppBlock> observable = nervosj.replayBlocksObservable(
                new DefaultBlockParameterNumber(BigInteger.ZERO),
                new DefaultBlockParameterNumber(BigInteger.valueOf(2)),
                false);

        final CountDownLatch transactionLatch = new CountDownLatch(appBlocks.size());
        final CountDownLatch completedLatch = new CountDownLatch(1);

        final List<AppBlock> results = new ArrayList<>(appBlocks.size());
        Subscription subscription = observable.subscribe(
                new Action1<AppBlock>() {
                    @Override
                    public void call(AppBlock result) {
                        results.add(result);
                        transactionLatch.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        fail(throwable.getMessage());
                    }
                },
                new Action0() {
                    @Override
                    public void call() {
                        completedLatch.countDown();
                    }
                });

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(appBlocks));

        subscription.unsubscribe();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isUnsubscribed());
    }

    @Test
    public void testReplayBlocksDescendingObservable() throws Exception {

        List<AppBlock> appBlocks = Arrays.asList(createBlock(2), createBlock(1), createBlock(0));

        OngoingStubbing<AppBlock> stubbing =
                when(nervosjService.send(any(Request.class), eq(AppBlock.class)));
        for (AppBlock appBlock : appBlocks) {
            stubbing = stubbing.thenReturn(appBlock);
        }

        Observable<AppBlock> observable = nervosj.replayBlocksObservable(
                new DefaultBlockParameterNumber(BigInteger.ZERO),
                new DefaultBlockParameterNumber(BigInteger.valueOf(2)),
                false, false);

        final CountDownLatch transactionLatch = new CountDownLatch(appBlocks.size());
        final CountDownLatch completedLatch = new CountDownLatch(1);

        final List<AppBlock> results = new ArrayList<>(appBlocks.size());
        Subscription subscription = observable.subscribe(
                new Action1<AppBlock>() {
                    @Override
                    public void call(AppBlock result) {
                        results.add(result);
                        transactionLatch.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        fail(throwable.getMessage());
                    }
                },
                new Action0() {
                    @Override
                    public void call() {
                        completedLatch.countDown();
                    }
                });

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(appBlocks));

        subscription.unsubscribe();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isUnsubscribed());
    }

    @Test
    public void testCatchUpToLatestAndSubscribeToNewBlockObservable() throws Exception {
        List<AppBlock> expected = Arrays.asList(
                createBlock(0), createBlock(1), createBlock(2),
                createBlock(3), createBlock(4), createBlock(5),
                createBlock(6));

        List<AppBlock> appBlocks = Arrays.asList(
                expected.get(2),  // greatest block
                expected.get(0), expected.get(1), expected.get(2),
                expected.get(4), // greatest block
                expected.get(3), expected.get(4),
                expected.get(4),  // greatest block
                expected.get(5),  // initial response from ethGetFilterLogs call
                expected.get(6)); // subsequent block from new block observable

        OngoingStubbing<AppBlock> stubbing =
                when(nervosjService.send(any(Request.class), eq(AppBlock.class)));
        for (AppBlock appBlock : appBlocks) {
            stubbing = stubbing.thenReturn(appBlock);
        }

        AppFilter appFilter = objectMapper.readValue(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}", AppFilter.class);
        AppLog appLog = objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":["
                        + "\"0x31c2342b1e0b8ffda1507fbffddf213c4b3c1e819ff6a84b943faabb0ebf2403\""
                        + "]}",
                AppLog.class);
        AppUninstallFilter appUninstallFilter = objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", AppUninstallFilter.class);

        when(nervosjService.send(any(Request.class), eq(AppFilter.class)))
                .thenReturn(appFilter);
        when(nervosjService.send(any(Request.class), eq(AppLog.class)))
                .thenReturn(appLog);
        when(nervosjService.send(any(Request.class), eq(AppUninstallFilter.class)))
                .thenReturn(appUninstallFilter);

        Observable<AppBlock> observable = nervosj.catchUpToLatestAndSubscribeToNewBlocksObservable(
                new DefaultBlockParameterNumber(BigInteger.ZERO),
                false);

        final CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        final CountDownLatch completedLatch = new CountDownLatch(1);

        final List<AppBlock> results = new ArrayList<>(expected.size());
        Subscription subscription = observable.subscribe(
                new Action1<AppBlock>() {
                    @Override
                    public void call(AppBlock result) {
                        results.add(result);
                        transactionLatch.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        fail(throwable.getMessage());
                    }
                },
                new Action0() {
                    @Override
                    public void call() {
                        completedLatch.countDown();
                    }
                });

        transactionLatch.await(1250, TimeUnit.MILLISECONDS);
        assertThat(results, equalTo(expected));

        subscription.unsubscribe();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isUnsubscribed());
    }

    private AppBlock createBlock(int number) {
        AppBlock appBlock = new AppBlock();
        AppBlock.Block block = new AppBlock.Block();
        AppBlock.Header header = new AppBlock.Header();
        header.setNumber(Numeric.encodeQuantity(BigInteger.valueOf(number)));
        block.setHeader(header);

        appBlock.setResult(block);
        return appBlock;
    }
}
