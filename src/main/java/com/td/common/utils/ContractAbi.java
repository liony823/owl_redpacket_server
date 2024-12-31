package com.td.common.utils;

import lombok.Data;

import java.util.List;

@Data
public class ContractAbi {
    private List<ContractFunction> functions;
    private List<ContractEvent> events;

    public ContractAbi(List<ContractFunction> functions, List<ContractEvent> events) {
        this.functions = functions;
        this.events = events;
    }

    public List<ContractFunction> getFunctions() {
        return functions;
    }

    public List<ContractEvent> getEvents() {
        return events;
    }
}