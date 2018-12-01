package org.nervos.appchain.tests;

import io.reactivex.Flowable;
import org.nervos.appchain.protocol.AppChainj;

public class BlockFilterFlowableExample {
    private static AppChainj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
    }

    public static void main(String[] args) {
        Flowable blockFitlerFlowable = service.appBlockHashFlowable();
        blockFitlerFlowable.subscribe(block ->
                System.out.println(block.toString())
        );
    }
}

