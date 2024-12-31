package com.td.common.utils;

import lombok.Data;

@Data
public class EthereumAddress {
    private String address;

    public EthereumAddress(String address) {
        if (address == null || address.length() != 42 || !address.startsWith("0x")) {
            throw new IllegalArgumentException("Invalid Ethereum address");
        }
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}