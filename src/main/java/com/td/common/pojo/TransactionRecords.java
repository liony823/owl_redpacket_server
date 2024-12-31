package com.td.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-06 18:13
 */
@Data
@Accessors(chain = true)
public class TransactionRecords implements Serializable {

    /**
     * {
     * "code": 0,
     * "msg": "ok",
     * "result": {
     * "hash": "0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501",
     * "blockNumber": "0x484ff9",
     * "blockHash": "0x2a569be8d1cddce3ea80422d3f8fe555af2a0e0791e900e2ad96e3ef51e60aab",
     * "from": "0xf0413fe3410657ab377e73ebf0028c45fab29b7b",
     * "to": "0x1e27bdf3a7b074f4d026b371b788f1625c95ee9c",
     * "value": "0x1bc16d674ec80000",
     * "gas": "0x5208",
     * "gasPrice": "0x77359400",
     * "time": "0x66b18a83",
     * "input": "0x"
     * }
     * }
     */

    private String hash;
    private String blockNumber;
    private String blockHash;
    private String from;
    private String to;
    private String value;
    private String gas;
    private String gasPrice;
    private String time;
    private String input;
    private String token;

}
