package org.nervos.appchain.tests;

import org.nervos.appchain.protocol.Nervosj;

import rx.Observable;

public class BlockFilterObservableExample {
    private static Nervosj service;

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

