package com.td.client.enums;

public enum RedPacketConfigKeyEnums {

    BASE_URL("base_url", "中心服务器请求路径"),
    CENTER_SERVER_ADDRESS("center_server_address", "中心服务器钱包地址"),
    CENTER_SERVER_PRIVATE_KEY("center_server_private_key", "中心服务器钱包私钥"),;

    private String key;
    private String description;

    RedPacketConfigKeyEnums(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }
}
