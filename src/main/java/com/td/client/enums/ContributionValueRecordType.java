package com.td.client.enums;

public enum ContributionValueRecordType {
    /**
     * 每日签到
     */
    SIGN_IN("sign_in", "每日签到"),

    /**
     * 连续7天签到
     */
    CONTINUOUS_SIGN_IN("continuous_sign_in", "连续7天签到"),

    /**
     * 在线30分钟
     */
    ONLINE_TIME_THIRTY_MINUTES("online_time_thirty_minutes", "在线30分钟"),

    /**
     * 每天在线60分钟
     */
    ONLINE_TIME_SIXTY_MINUTES("online_time_sixty_minutes", "每天在线60分钟"),

    /**
     * 在线120分钟
     */
    ONLINE_TIME_ONE_HUNDRED_TWENTY_MINUTES("online_time_one_hundred_twenty_minutes", "在线120分钟"),

    /**
     * 在线180分钟
     */
    ONLINE_TIME_ONE_HUNDRED_EIGHTY_MINUTES("online_time_one_hundred_eighty_minutes", "在线180分钟"),

    /**
     * 红包余额激励
     */
    RED_PACKET_BALANCE_INCENTIVE("red_packet_balance_incentive", "红包余额激励"),

    /**
     * 红包余额兑换
     */
    EXCHANGE_OWL("exchange_owl", "兑换owl"),

    /**
     * owl兑换汇率
     */
    OWL_EXCHANGE_RATE("owl_exchange_rate", "owl兑换汇率"),

    ;

    private String code;
    private String desc;

    ContributionValueRecordType(String code, String desc) {
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
