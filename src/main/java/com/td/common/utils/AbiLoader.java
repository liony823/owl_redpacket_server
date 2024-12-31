package com.td.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AbiLoader {
    public static ContractAbi loadContractAbi(String abiFilePath) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = AbiLoader.class.getClassLoader().getResourceAsStream(abiFilePath);

        if (inputStream == null) {
            throw new RuntimeException("ABI file not found: " + abiFilePath);
        }

        // Parse ABI JSON
        List<Map<String, Object>> abiJson = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        // Convert ABI JSON to ContractAbi
        List<ContractFunction> functions = parseFunctions(abiJson);
        List<ContractEvent> events = parseEvents(abiJson);

        return new ContractAbi(functions, events);
    }

    private static List<ContractFunction> parseFunctions(List<Map<String, Object>> abiJson) {
        List<ContractFunction> functions = new ArrayList<>();
        for (Map<String, Object> entry : abiJson) {
            if ("function".equals(entry.get("type"))) {
                ContractFunction function = new ContractFunction();
                function.setName((String) entry.get("name"));
                function.setInputs((List<String>) entry.get("inputs"));
                function.setOutputs((List<String>) entry.get("outputs"));
                function.setStateMutability((String) entry.get("stateMutability"));
                function.setType((String) entry.get("type"));
                functions.add(function);
            }
        }
        return functions;
    }

    private static List<ContractEvent> parseEvents(List<Map<String, Object>> abiJson) {
        List<ContractEvent> events = new ArrayList<>();
        for (Map<String, Object> entry : abiJson) {
            if ("event".equals(entry.get("type"))) {
                ContractEvent event = new ContractEvent();
                event.setName((String) entry.get("name"));
                event.setInputs((List<String>) entry.get("inputs"));
                event.setType((String) entry.get("type"));
                event.setAnonymous((Boolean) entry.get("anonymous"));
                events.add(event);
            }
        }
        return events;
    }
}