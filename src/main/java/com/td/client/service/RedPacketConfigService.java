package com.td.client.service;

public interface RedPacketConfigService {

    void initConfigCache();

    String getConfig(String configKey);
}
