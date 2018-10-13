package org.nervos.appchain.protocol.core.filters;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.nervos.appchain.protocol.AppChainj;
import org.nervos.appchain.protocol.AppChainjService;

import org.nervos.appchain.protocol.ObjectMapperFactory;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.response.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.AppLog;
import org.nervos.appchain.protocol.core.methods.response.AppUninstallFilter;
import rx.Observable;
import rx.Subscription;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class FilterTester {

    private AppChainjService appChainjService;
    AppChainj appChainj;

    final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    @Before
    public void setUp() {
        appChainjService = mock(AppChainjService.class);
        appChainj = AppChainj.build(appChainjService, 1000, scheduledExecutorService);
    }

    <T> void runTest(AppLog appLog, Observable<T> observable) throws Exception {
        AppFilter appFilter = objectMapper.readValue(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}", AppFilter.class);

        AppUninstallFilter appUninstallFilter = objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", AppUninstallFilter.class);

        @SuppressWarnings("unchecked")
        List<T> expected = createExpected(appLog);
        Set<T> results = Collections.synchronizedSet(new HashSet<T>());

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());

        CountDownLatch completedLatch = new CountDownLatch(1);

        when(appChainjService.send(any(Request.class), eq(AppFilter.class)))
                .thenReturn(appFilter);
        when(appChainjService.send(any(Request.class), eq(AppLog.class)))
                .thenReturn(appLog);
        when(appChainjService.send(any(Request.class), eq(AppUninstallFilter.class)))
                .thenReturn(appUninstallFilter);

        Subscription subscription = observable.subscribe(
                result -> {
                    results.add(result);
                    transactionLatch.countDown();
                },
                throwable -> fail(throwable.getMessage()),
                () -> completedLatch.countDown());

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(new HashSet<>(expected)));

        subscription.unsubscribe();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isUnsubscribed());
    }

    List createExpected(AppLog appLog) {
        List<AppLog.LogResult> logResults = appLog.getLogs();
        if (logResults.isEmpty()) {
            fail("Results cannot be empty");
        }

        return appLog.getLogs().stream()
                .map(t -> t.get()).collect(Collectors.toList());
    }
}
