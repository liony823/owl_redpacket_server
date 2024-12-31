package com.td.client.enums;

import lombok.Data;

public enum RedPacketType {
    PRIVATE("private", "个人红包"),
    LUCK("luck", "群幸运红包"),
    EXCLUSIVE("exclusive", "群专属红包");

    private String code;
    private String desc;

    RedPacketType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    //根据code获取枚举
    public static RedPacketType getEnumByCode(String code) {
        for (RedPacketType redPacketType : RedPacketType.values()) {
            if (redPacketType.getCode().equals(code)) {
                return redPacketType;
            }
        }
        return null;
    }

}
