/*
 Navicat Premium Data Transfer

 Source Server         : mongo
 Source Server Type    : MongoDB
 Source Server Version : 40429 (4.4.29)
 Source Host           : 127.0.0.1:27017
 Source Schema         : openim_v3

 Target Server Type    : MongoDB
 Target Server Version : 40429 (4.4.29)
 File Encoding         : 65001

 Date: 12/09/2024 15:24:57
*/


// ----------------------------
// Collection structure for contribution_value_config
// ----------------------------
db.getCollection("contribution_value_config").drop();
db.createCollection("contribution_value_config");

// ----------------------------
// Documents of contribution_value_config
// ----------------------------
db.getCollection("contribution_value_config").insert([ {
    _id: ObjectId("66dbbdbe788e772ebf04b0c2"),
    configKey: "normal_sign_in",
    configValue: "1",
    description: "普通签到贡献值奖励"
} ]);
db.getCollection("contribution_value_config").insert([ {
    _id: ObjectId("66dbbe19788e772ebf04b0c3"),
    configKey: "continuous_sign_in",
    configValue: "4",
    description: "连续签到贡献值奖励"
} ]);
db.getCollection("contribution_value_config").insert([ {
    _id: ObjectId("66dbbe70788e772ebf04b0c4"),
    configKey: "online_time:30",
    configValue: "3",
    description: "在线三十分钟贡献值奖励"
} ]);
db.getCollection("contribution_value_config").insert([ {
    _id: ObjectId("66dbbe80788e772ebf04b0c5"),
    configKey: "online_time:60",
    configValue: "7",
    description: "在线六十分钟贡献值奖励"
} ]);
db.getCollection("contribution_value_config").insert([ {
    _id: ObjectId("66dbbe94788e772ebf04b0c6"),
    configKey: "online_time:120",
    configValue: "15",
    description: "在线两小时贡献值奖励"
} ]);
db.getCollection("contribution_value_config").insert([ {
    _id: "66dbbea9788e772ebf04b0c8",
    configKey: "red_packet_balance_incentive",
    configValue: "0.001",
    description: "红包余额激励贡献值奖励"
} ]);
db.getCollection("contribution_value_config").insert([ {
    _id: "66dbbf18788e772ebf04b0c9",
    configKey: "owl_exchange_rate",
    configValue: "58",
    description: "owl兑换汇率"
} ]);
