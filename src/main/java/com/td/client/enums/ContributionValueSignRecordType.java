package com.td.client.enums;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-09-07 9:55
 */
public enum ContributionValueSignRecordType {

    /**
     * 普通签到
     */
    NORMAL_SIGN_IN("normal_sign_in", "普通签到"),

    /**
     * 连续签到
     */
    CONTINUOUS_SIGN_IN("continuous_sign_in", "连续签到");

    private String code;
    private String desc;

    ContributionValueSignRecordType(String code, String desc) {
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
