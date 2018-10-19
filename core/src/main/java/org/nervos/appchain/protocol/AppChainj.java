package org.nervos.appchain.protocol;

import java.util.concurrent.ScheduledExecutorService;

import org.nervos.appchain.protocol.core.AppChain;
import org.nervos.appchain.protocol.core.JsonRpc2_0AppChainj;
import org.nervos.appchain.protocol.rx.AppChainjRx;

/**
 * JSON-RPC Request object building factory.
 */
public interface AppChainj extends AppChain, AppChainjRx {

    /**
     * Construct a new AppChainj instance.
     *
     * @param appChainjService appChainj service instance - i.e. HTTP or IPC
     * @return new AppChainj instance
     */
    static AppChainj build(AppChainjService appChainjService) {
        return new JsonRpc2_0AppChainj(appChainjService);
    }

    static AppChainj build(AppChainjService appChainjService, long pollingInterval) {
        return new JsonRpc2_0AppChainj(appChainjService, pollingInterval);
    }

    /**
     * Construct a new AppChainj instance.
     *
     * @param appChainjService appChainj service instance - i.e. HTTP or IPC
     * @param pollingInterval polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks.
     *                                 <strong>You are responsible for terminating this thread
     *                                 pool</strong>
     * @return new AppChainj instance
     */
    static AppChainj build(
            AppChainjService appChainjService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0AppChainj(appChainjService, pollingInterval, scheduledExecutorService);
    }
}
