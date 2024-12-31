package com.td.common.utils;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class ContractUtils {

    public static String encodeCall(String functionName, String fromAddress, String toAddress, BigInteger amount) {
        // Create the Function object with parameters
        Function function = new Function(
                functionName, // Function name
                Arrays.asList(
                        new Address(fromAddress), // Address parameter
                        new Address(toAddress),   // Address parameter
                        new Uint256(amount)        // Uint256 parameter
                ),
                Collections.emptyList() // No output parameters needed
        );

        // Encode the function call data
        return FunctionEncoder.encode(function);
    }
}