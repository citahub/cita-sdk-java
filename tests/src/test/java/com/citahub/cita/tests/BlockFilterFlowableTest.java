package com.citahub.cita.tests;

import com.citahub.cita.protocol.CITAj;
import io.reactivex.Flowable;
import org.junit.Test;

public class BlockFilterFlowableTest {
    public static CITAj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
    }

    @Test
    public void testBlockFilterFlowable( ) {
        Flowable blockFitlerFlowable = service.appBlockHashFlowable();
        blockFitlerFlowable.subscribe(block ->
                System.out.println(block.toString())
        );
    }
}

