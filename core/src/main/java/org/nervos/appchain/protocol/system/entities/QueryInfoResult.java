package org.nervos.appchain.protocol.system.entities;

import java.util.List;

public class QueryInfoResult {
    public String name;
    public List<String> contractAddrs;
    public List<String> funcs;

    public QueryInfoResult(String name, List<String> contractAddrs, List<String> funcs) {
        this.name = name;
        this.contractAddrs = contractAddrs;
        this.funcs = funcs;
    }
}
