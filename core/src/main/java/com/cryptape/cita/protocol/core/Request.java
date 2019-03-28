package com.cryptape.cita.protocol.core;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Flowable;
import com.cryptape.cita.protocol.CITAjService;

public class Request<S, T extends Response> {
    private static AtomicLong nextId = new AtomicLong(0);

    private String jsonrpc = "2.0";
    private String method;
    private List<S> params;
    private long id;

    private CITAjService CITAjService;

    private Class<T> responseType;

    public Request() {
    }

    public Request(String method, List<S> params,
                   CITAjService CITAjService, Class<T> type) {
        this.method = method;
        this.params = params;
        this.id = nextId.getAndIncrement();
        this.CITAjService = CITAjService;
        this.responseType = type;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<S> getParams() {
        return params;
    }

    public void setParams(List<S> params) {
        this.params = params;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public T send() throws IOException {
        return CITAjService.send(this, responseType);
    }

    public Future<T> sendAsync() {
        return  CITAjService.sendAsync(this, responseType);
    }

    public Flowable<T> flowable() {
        return new RemoteCall<T>(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return Request.this.send();
            }
        }).flowable();
    }
}
