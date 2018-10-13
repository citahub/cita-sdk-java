package org.nervos.appchain.tests;

import org.nervos.appchain.protocol.AppChainj;

import rx.Observable;

public class BlockFilterObservableExample {
    private static AppChainj service;

    static {
        Config conf = new Config();
        conf.buildService(false);
        service = conf.service;
    }

    public static void main(String[] args) {
        Observable blockFitlerObservable = service.appBlockHashObservable();
        blockFitlerObservable.subscribe(block ->
                System.out.println(block.toString())
        );
    }
}

