package com.citahub.cita.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.citahub.cita.protocol.core.Request;
import com.citahub.cita.protocol.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Flowable;
import io.reactivex.Notification;
import com.citahub.cita.utils.Async;

/**
 * Base service implementation.
 */
public abstract class Service implements CITAjService {

    protected final ObjectMapper objectMapper;

    public Service(boolean includeRawResponses) {
        objectMapper = ObjectMapperFactory.getObjectMapper(includeRawResponses);
    }

    protected abstract InputStream performIO(String payload) throws IOException;

    @Override
    public <T extends Response> T send(
            Request request, Class<T> responseType) throws IOException {
        String payload = objectMapper.writeValueAsString(request);
        InputStream result = null;
        try {
            result = performIO(payload);
            if (result != null) {
                return objectMapper.readValue(result, responseType);
            } else {
                return null;
            }
        }finally {
            if(result != null)
                result.close();
        }
    }

    @Override
    public <T extends Response> Future<T> sendAsync(
            final Request jsonRpc20Request, final Class<T> responseType) {
        return Async.run(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return Service.this.send(jsonRpc20Request, responseType);
            }
        });
    }

    @Override
    public <T extends Notification<?>> Flowable<T> subscribe(
            Request request,
            String unsubscribeMethod,
            Class<T> responseType) {
        throw new UnsupportedOperationException(
                String.format(
                        "Service %s does not support subscriptions",
                        this.getClass().getSimpleName()));
    }
}
