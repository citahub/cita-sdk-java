package com.citahub.cita.protocol.system.entities;

import java.util.List;

public class QueryResourceResult {
    public List<String> contractAddrs;
    public List<String> funcs;

    public QueryResourceResult(List<String> contractAddrs, List<String> funcs) {
        this.contractAddrs = contractAddrs;
        this.funcs = funcs;
    }
}
