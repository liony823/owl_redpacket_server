/*
 Navicat Premium Data Transfer

 Source Server         : owl-prod
 Source Server Type    : MongoDB
 Source Server Version : 60002 (6.0.2)
 Source Host           : 43.198.43.133:37017
 Source Schema         : owlim_v1

 Target Server Type    : MongoDB
 Target Server Version : 60002 (6.0.2)
 File Encoding         : 65001

 Date: 09/09/2024 17:27:21
*/


// ----------------------------
// Collection structure for red_packet_config
// ----------------------------
db.getCollection("red_packet_config").drop();
db.createCollection("red_packet_config");

// ----------------------------
// Documents of red_packet_config
// ----------------------------
db.getCollection("red_packet_config").insert([ {
    _id: ObjectId("66b19ca01fca7013d7160db7"),
    configKey: "base_url",
    configValue: "http://52.220.39.39:9020",
    description: "中心服务器请求路径",
    _class: "com.td.common.pojo.RedPacketConfig"
} ]);
db.getCollection("red_packet_config").insert([ {
    _id: ObjectId("66b19d695e45ac2d6879f0a3"),
    configKey: "center_server_address",
    configValue: "0xf0413fe3410657ab377e73ebf0028c45fab29b7b",
    description: "中心服务器钱包地址",
    _class: "com.td.common.pojo.RedPacketConfig"
} ]);
db.getCollection("red_packet_config").insert([ {
    _id: ObjectId("66b19d695e45ac2d6879f0a4"),
    configKey: "center_server_private_key",
    configValue: "35da9ac643c66739a9ac2e35c799f63080b880ce28836934e32d4168c745fe67",
    description: "中心服务器钱包私钥",
    _class: "com.td.common.pojo.RedPacketConfig"
} ]);
db.getCollection("red_packet_config").insert([ {
    _id: "66debf3bfe0eb0a0ea0c2151",
    configKey: "page_size",
    configValue: "100",
    description: "分页参数",
    _class: "com.td.common.pojo.RedPacketConfig"
} ]);
