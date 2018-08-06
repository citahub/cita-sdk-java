package org.nervos.appchain.protocol.core.methods.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.nervos.appchain.utils.Numeric;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Call {
    private String from;
    private String to;
    private String data;


    public Call(String from, String to, String data) {
        this.from = from;
        this.to = to;

        if (data != null) {
            this.data = Numeric.prependHexPrefix(data);
        }
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getData() {
        return data;
    }
}
