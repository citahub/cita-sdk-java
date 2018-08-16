package org.nervos.appchain.protocol;

import org.nervos.appchain.protocol.core.JsonRpc2_0Web3j;

import java.util.concurrent.ScheduledExecutorService;

public class NervosjFactory {
    /**
     * Construct a new Web3j instance.
     *
     * @param nervosjService nervosj service instance - i.e. HTTP or IPC
     * @return new Nervosj instance
     */
    public static Nervosj build(NervosjService nervosjService) {
        return new JsonRpc2_0Web3j(nervosjService);
    }

    /**
     * Construct a new Web3j instance.
     *
     * @param nervosjService nervosj service instance - i.e. HTTP or IPC
     * @param pollingInterval polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks.
     *                                 <strong>You are responsible for terminating this thread
     *                                 pool</strong>
     * @return new Web3j instance
     */
    public static Nervosj build(
            NervosjService nervosjService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Web3j(nervosjService, pollingInterval, scheduledExecutorService);
    }
}
