package org.nervos.appchain.protocol;

import java.util.concurrent.ScheduledExecutorService;

import org.nervos.appchain.protocol.core.AppChain;
import org.nervos.appchain.protocol.core.JsonRpc2_0Web3j;
import org.nervos.appchain.protocol.rx.NervosjRx;

/**
 * JSON-RPC Request object building factory.
 */
public interface Nervosj extends AppChain, NervosjRx {

    /**
     * Construct a new Nervosj instance.
     *
     * @param nervosjService nervosj service instance - i.e. HTTP or IPC
     * @return new Nervosj instance
     */
    static Nervosj build(NervosjService nervosjService) {
        return new JsonRpc2_0Web3j(nervosjService);
    }

    static Nervosj build(NervosjService nervosjService, long pollingInterval) {
        return new JsonRpc2_0Web3j(nervosjService, pollingInterval);
    }

    /**
     * Construct a new Nervosj instance.
     *
     * @param nervosjService nervosj service instance - i.e. HTTP or IPC
     * @param pollingInterval polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks.
     *                                 <strong>You are responsible for terminating this thread
     *                                 pool</strong>
     * @return new Nervosj instance
     */
    static Nervosj build(
            NervosjService nervosjService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Web3j(nervosjService, pollingInterval, scheduledExecutorService);
    }
}
