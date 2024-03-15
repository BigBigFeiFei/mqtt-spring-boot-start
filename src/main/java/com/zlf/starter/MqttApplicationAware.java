package com.zlf.starter;

import com.alibaba.fastjson.JSON;
import com.zlf.config.MqttConfig;
import com.zlf.config.MqttProperties;
import com.zlf.event.MessageArrivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zlf
 * 下面注释的代码是可以和MqttConfig、MqttProperties、MqttUtil工具类配置使用,其它代码删除即可，
 * 由于做成一个start启动器可以直接使用,不用MqttUtil工具，
 * MqttUtil可以单独使用不依赖以nacos相关的依赖，该start也可以不依赖nacos相关依赖,
 * 不依赖于nacos的配置动态感知刷新可以移除@RefreshScope相关的主机即可。
 * 配置信息从项目的配置文件中读取即可
 */
@Component
@Slf4j
public class MqttApplicationAware implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("==========Mqtt启动初始化开始===========");
        MqttConfig mqttConfig = applicationContext.getBean(MqttConfig.class);
        List<MqttProperties> mps = mqttConfig.getMps();
        if (CollectionUtils.isNotEmpty(mps)) {
            for (MqttProperties mp : mps) {
                log.info("==========Mqtt启动初始化配置:{}==========", JSON.toJSONString(mp));
                //MqttClient mqttClient = MqttUtil.createMqttClient2(mp.getBrokers(), mp.getUserName(), mp.getPassword(), mp.getClientId());
                MqttClient mqttClient = (MqttClient) applicationContext.getBean(mp.getClientId());
                MqttConnectOptions mqttConnectOption = (MqttConnectOptions) applicationContext.getBean(MqttClientRegistrar.MQTT_OPS_PREFIX + mp.getClientId());
                try {
                    mqttClient.connect(mqttConnectOption);
                } catch (MqttException e) {
                    log.error("Mqtt启动连接异常ex:{}", e.getMessage());
                }
                if ("subscribe".equals(mp.getType())) {
                    //MqttUtil.subscribe(mqttClient, mp.getTopic(), mp.getQos());
                    try {
                        mqttClient.subscribe(mp.getTopic(), mp.getQos());
                    } catch (MqttException e) {
                        log.error("Mqtt启动订阅异常ex:{}", e.getMessage());
                    }
                    log.info("==========Mqtt启动初始化订阅配置完成==========");
                }
                mqttClient.setCallback(new MqttCallback() {

                    public void connectionLost(Throwable cause) {
                        log.error("connectionLost:{}", cause.getMessage());
                    }

                    public void messageArrived(String topic, MqttMessage message) {
                        String msg = new String(message.getPayload());
                        int qos1 = message.getQos();
                        log.info("subscribe topic:{}", topic);
                        log.info("subscribe Qos:{}", qos1);
                        log.info("subscribe msg:{}", msg);
                        //新增业务拓展接口对接或者是发springEvent,业务监听该消息处理业务即可,这里采用事件监听的方式
                        applicationContext.publishEvent(new MessageArrivedEvent(this, topic, message));
                    }

                    public void deliveryComplete(IMqttDeliveryToken token) {
                        log.info("delivery complete:{}", token.isComplete());
                    }

                });
            }
        }
        log.info("==========Mqtt启动初始化结束===========");
    }

}
