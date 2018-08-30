package org.nervos.appchain.tests;

import java.util.Properties;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.http.HttpService;

import rx.Observable;

public class BlockFilterObservableExample {
    private static Properties props;
    private static String testNetIpAddr;
    private static Nervosj service;

    static {
        props = Config.load();
        testNetIpAddr = props.getProperty(Config.TEST_NET_ADDR);

        HttpService.setDebug(false);
        service = Nervosj.build(new HttpService(testNetIpAddr));
    }

    public static void main(String[] args) {
        Observable blockFitlerObservable = service.appBlockHashObservable();
        blockFitlerObservable.subscribe(block -> {
            System.out.println(block.toString());
        });
    }
}

