package com.td.common.utils;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class DeployedContract {
    private final ContractAbi abi;
    private final EthereumAddress address;

    // 构造函数
    public DeployedContract(ContractAbi abi, EthereumAddress address) {
        this.abi = abi;
        this.address = address;
    }

    public ContractAbi getAbi() {
        return abi;
    }

    public EthereumAddress getAddress() {
        return address;
    }

    // 获取合约的所有函数
    public List<ContractFunction> getFunctions() {
        return abi.getFunctions();
    }

    // 获取合约的所有事件
    public List<ContractEvent> getEvents() {
        return abi.getEvents();
    }

    // 查找合约中名称匹配的所有函数
    public List<ContractFunction> findFunctionsByName(String name) {
        return getFunctions().stream()
            .filter(f -> f.getName().equals(name))
            .collect(Collectors.toList());
    }

    // 查找合约中名称匹配的唯一函数
    public ContractFunction function(String name) {
        List<ContractFunction> functions = findFunctionsByName(name);
        if (functions.size() != 1) {
            throw new IllegalStateException("Function not found or multiple functions found");
        }
        return functions.get(0);
    }

    // 查找合约中名称匹配的唯一事件
    public ContractEvent event(String name) {
        List<ContractEvent> events = getEvents().stream()
            .filter(e -> e.getName().equals(name))
            .collect(Collectors.toList());
        if (events.size() != 1) {
            throw new IllegalStateException("Event not found or multiple events found");
        }
        return events.get(0);
    }

    // 获取所有构造函数
//    public List<ContractFunction> getConstructors() {
//        return getFunctions().stream()
//            .filter(ContractFunction::isConstructor)
//            .collect(Collectors.toList());
//    }
}