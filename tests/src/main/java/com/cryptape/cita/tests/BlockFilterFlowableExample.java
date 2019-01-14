package com.cryptape.cita.tests;

import com.cryptape.cita.protocol.CITAj;
import io.reactivex.Flowable;

public class BlockFilterFlowableExample {
    private static CITAj service;

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

