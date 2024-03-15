package com.zlf.config;

import lombok.Data;

import java.util.List;

/**
 * 其它属性默认即可,可以抽成配置类的参数
 * @author zlf
 */
@Data
public class MqttProperties {

    /**
     * mqtt服务器地址列表
     */
    private List<String> brokers;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 客户端clientId
     */
    private String clientId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 消息等级
     * QoS 0：最多交付一次，消息可能丢失。
     * QoS 1：至少交付一次，消息可以保证到达，但是可能重复到达。
     * QoS 2：只交付一次，消息保证到达，并且不会重复。
     */
    private Integer qos;

    /**
     * 类型：只支持下面两种类型,如果要支持发布和订阅可以配置多个,只不过类型不一样而已
     * publish 发布
     * subscribe 订阅
     */
    private String type;

}
