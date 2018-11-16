package org.nervos.appchain.abi;

import java.util.ArrayList;
import java.util.List;

import org.nervos.appchain.abi.datatypes.Event;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.UnorderedEvent;
import org.nervos.appchain.crypto.Hash;
import org.nervos.appchain.utils.Numeric;

/**
 * <p>Ethereum filter encoding.
 * Further limited details are available
 * <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#events">here</a>.
 * </p>
 */
public class EventEncoder {

    private EventEncoder() { }

    public static String encode(Event function) {
        List<TypeReference<Type>> indexedParameters = function.getIndexedParameters();
        List<TypeReference<Type>> nonIndexedParameters = function.getNonIndexedParameters();

        String methodSignature = buildMethodSignature(function.getName(),
                indexedParameters, nonIndexedParameters);

        return buildEventSignature(methodSignature);
    }

    public static String encode(UnorderedEvent event) {
        List<TypeReference<Type>> parameters = event.getParameters();
        String methodSignature = buildMethodSignature(event.getName(), parameters);
        return buildEventSignature(methodSignature);
    }

    static <T extends Type> String buildMethodSignature(
            String methodName, List<TypeReference<T>> indexParameters) {
        List<TypeReference<T>> parameters = new ArrayList<TypeReference<T>>(indexParameters);

        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");


        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            params.append(Utils.getTypeName(parameters.get(i)));
            if (i + 1 < parameters.size()) {
                params.append(",");
            }
        }

        result.append(params.toString());
        result.append(")");
        return result.toString();
    }

    static <T extends Type> String buildMethodSignature(
            String methodName, List<TypeReference<T>> indexParameters,
            List<TypeReference<T>> nonIndexedParameters) {

        List<TypeReference<T>> parameters = new ArrayList<TypeReference<T>>(indexParameters);
        parameters.addAll(nonIndexedParameters);

        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");


        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            params.append(Utils.getTypeName(parameters.get(i)));
            if (i + 1 < parameters.size()) {
                params.append(",");
            }
        }

        result.append(params.toString());
        result.append(")");
        return result.toString();
    }

    public static String buildEventSignature(String methodSignature) {
        byte[] input = methodSignature.getBytes();
        byte[] hash = Hash.sha3(input);
        return Numeric.toHexString(hash);
    }
}
