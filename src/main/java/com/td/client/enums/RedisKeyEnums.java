package com.td.client.enums;

public enum RedisKeyEnums {

    /**
     * 红包余额
     */
    USER_BALANCE("red-packet:balance:", "红包余额"),

    /**
     * 红包充值
     */
    RED_PACKET_RECHARGE("red-packet:recharge:", "红包充值"),

    /**
     * 红包提现
     */
    RED_PACKET_WITHDRAW("red-packet:withdraw:", "红包提现"),

    /**
     * 个人红包
     */
    RED_PACKET_PRIVATE("red-packet:private:", "个人红包"),

    /**
     * 群幸运红包
     */

    RED_PACKET_LUCK("red-packet:luck:", "群幸运红包"),

    /**
     * 群专属红包
     */

    RED_PACKET_EXCLUSIVE("red-packet:exclusive:", "群专属红包"),

    /**
     * 红包领取
     */
    RED_PACKET_RECEIVE("red-packet:receive:", "红包领取"),

    /**
     * 红包领取人数
     */
    RED_PACKET_RECEIVE_COUNT("red-packet:receive-count:", "红包领取人数"),

    /**
     * 签到
     */
    SIGN("sign:", "签到"),

    /**
     * 贡献值余额
     */
    CONTRIBUTION_VALUE_BALANCE("contribution-value:balance:", "贡献值余额"),

    /**
     * 在线时长
     */
    ONLINE_TIME("online-time:", "在线时长"),
    ONLINE_TIME_FLAG("online-time-flag:", "在线时长标记"),
    RED_PACKET_BALANCE_INCENTIVE("red-packet:balance-incentive:", "红包余额激励");


    private String key;
    private String desc;

    RedisKeyEnums(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }
    }
