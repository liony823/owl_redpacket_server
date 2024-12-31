package com.td.client.enums;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-08-05 11:39
 */
public enum RedPacketRecordType {
    RECHARGE("recharge", "充值"),
    WITHDRAW("withdraw", "提取"),
    REFUND("refund", "红包退款"),
    SEND_TO_USER("send_to_user", "发送个人红包"),
    SEND_TO_GROUP("send_to_group", "发送到群"),
    RECEIVE_FROM_USER("receive_from_user", "接收个人红包"),
    RECEIVE_FROM_GROUP("receive_from_group", "领取群红包");


    private String code;
    private String desc;

    RedPacketRecordType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
