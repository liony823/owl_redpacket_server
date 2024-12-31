package com.td.common.utils;

import lombok.Data;

import java.util.List;

@Data
public class ContractEvent {
    private String name;
    private List<String> inputs;
    private String type;
    private boolean anonymous;

    // Constructor, getters, and setters
}