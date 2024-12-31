package com.td.common.utils;

import lombok.Data;

import java.util.List;

@Data
public class ContractFunction {
    private String name;
    private List<String> inputs;
    private List<String> outputs;
    private String stateMutability;
    private String type;
}