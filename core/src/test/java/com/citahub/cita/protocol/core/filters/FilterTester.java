package com.cryptape.cita.protocol.core.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cryptape.cita.protocol.core.methods.response.AppFilter;
import com.cryptape.cita.protocol.core.methods.response.AppLog;
import com.cryptape.cita.protocol.core.methods.response.AppUninstallFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.junit.Before;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.CITAjService;

import com.cryptape.cita.protocol.ObjectMapperFactory;
import com.cryptape.cita.protocol.core.Request;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class FilterTester {

    private CITAjService CITAjService;
    CITAj citaj;

    final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    @Before
    public void setUp() {
        CITAjService = mock(CITAjService.class);
        citaj = CITAj.build(CITAjService, 1000, scheduledExecutorService);
    }

    <T> void runTest(AppLog appLog, Flowable<T> flowable) throws Exception {
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

        when(CITAjService.send(any(Request.class), eq(AppFilter.class)))
                .thenReturn(appFilter);
        when(CITAjService.send(any(Request.class), eq(AppLog.class)))
                .thenReturn(appLog);
        when(CITAjService.send(any(Request.class), eq(AppUninstallFilter.class)))
                .thenReturn(appUninstallFilter);

        Disposable subscription = flowable.subscribe(
                result -> {
                    results.add(result);
                    transactionLatch.countDown();
                },
                throwable -> fail(throwable.getMessage()),
                () -> completedLatch.countDown());

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertThat(results, equalTo(new HashSet<>(expected)));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    List createExpected(AppLog appLog) {
        List<AppLog.LogResult> logResults = appLog.getLogs();
        if (logResults.isEmpty()) {
            fail("Results cannot be empty");
        }
        List list = new ArrayList();
        for (AppLog.LogResult logResult : appLog.getLogs()) {
            list.add(logResult.get());
        }
        return list;
    }
}
