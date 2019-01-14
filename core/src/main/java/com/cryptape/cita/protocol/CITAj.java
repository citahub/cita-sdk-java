package com.cryptape.cita.protocol;

import java.util.concurrent.ScheduledExecutorService;

import com.cryptape.cita.protocol.core.CITA;
import com.cryptape.cita.protocol.core.JsonRpc2_0CITAj;
import com.cryptape.cita.protocol.rx.CITAjRx;

/**
 * JSON-RPC Request object building factory.
 */
public interface CITAj extends CITA, CITAjRx {

    /**
     * Construct a new CITAj instance.
     *
     * @param citajService citaj service instance - i.e. HTTP or IPC
     * @return new CITAj instance
     */
    static CITAj build(CITAjService citajService) {
        return new JsonRpc2_0CITAj(citajService);
    }

    static CITAj build(CITAjService citajService, long pollingInterval) {
        return new JsonRpc2_0CITAj(citajService, pollingInterval);
    }

    /**
     * Construct a new CITAj instance.
     *
     * @param citajService citaj service instance - i.e. HTTP or IPC
     * @param pollingInterval polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks.
     *                                 <strong>You are responsible for terminating this thread
     *                                 pool</strong>
     * @return new CITAj instance
     */
    static CITAj build(
            CITAjService citajService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0CITAj(citajService, pollingInterval, scheduledExecutorService);
    }
}
