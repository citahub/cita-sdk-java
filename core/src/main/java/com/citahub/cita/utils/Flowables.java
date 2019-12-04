package com.citahub.cita.utils;

import java.math.BigInteger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

/**
 * Flowable utility functions.
 */
public class Flowables {

    public static Flowable<BigInteger> range(
            final BigInteger startValue, final BigInteger endValue) {
        return range(startValue, endValue, true);
    }

    /**
     * Simple {@link Flowable} implementation to emit a range of BigInteger values.
     *
     * @param startValue first value to emit in range
     * @param endValue   final value to emit in range
     * @param ascending  direction to iterate through range
     * @return a {@link Flowable} instance to emit this range of values
     */
    public static Flowable<BigInteger> range(
            final BigInteger startValue, final BigInteger endValue, final boolean ascending) {
        if (startValue.compareTo(BigInteger.ZERO) == -1) {
            throw new IllegalArgumentException("Negative start index cannot be used");
        } else if (startValue.compareTo(endValue) > 0) {
            throw new IllegalArgumentException(
                    "Negative start index cannot be greater then end index");
        }

        if (ascending) {
            return Flowable.create(new FlowableOnSubscribe<BigInteger>() {
                @Override
                public void subscribe(FlowableEmitter<BigInteger> emitter) throws Exception {
                    for (BigInteger i = startValue; i.compareTo(endValue) < 1 && !emitter.isCancelled(); i = i.add(BigInteger.ONE)) {
                        emitter.onNext(i);
                    }
                    if (!emitter.isCancelled()) {
                        emitter.onComplete();
                    }
                }
            }, BackpressureStrategy.BUFFER);
        } else {
            return Flowable.create(new FlowableOnSubscribe<BigInteger>() {
                @Override
                public void subscribe(FlowableEmitter<BigInteger> emitter) throws Exception {
                    for (BigInteger i = endValue;  i.compareTo(startValue) > -1 && !emitter.isCancelled(); i = i.subtract(BigInteger.ONE)) {
                        emitter.onNext(i);
                    }
                    if (!emitter.isCancelled()) {
                        emitter.onComplete();
                    }
                }
            }, BackpressureStrategy.BUFFER);
        }
    }
}
