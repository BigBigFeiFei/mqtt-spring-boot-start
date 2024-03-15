package com.zlf.starter;

import com.zlf.config.MqttConfig;
import com.zlf.config.MqttProperties;
import com.zlf.util.MqttSpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author zlf
 */
@Service
@Slf4j
public class MqttApiService {

    @Autowired
    private MqttConfig mqttConfig;

    @Autowired
    private MqttSpringUtils mqttSpringUtils;

    public MqttProperties getMqttProperties(String clientId) {
        List<MqttProperties> mps = mqttConfig.getMps();
        if (CollectionUtils.isNotEmpty(mps)) {
            for (MqttProperties mp : mps) {
                if (mp.getClientId().equals(clientId)) {
                    return mp;
                }
            }
        }
        return null;
    }

    public MqttClient getMqttClient(String clientId) {
        MqttClient mqttClient = (MqttClient) mqttSpringUtils.getBean(clientId);
        return mqttClient;
    }

    public MqttMessage createMessage(String topic, Integer qos, String content) {
        // 创建消息并设置 QoS
        MqttMessage message = new MqttMessage(content.getBytes());
        if (Objects.isNull(qos)) {
            //默认是1
            qos = 1;
            message.setQos(qos);
        } else {
            message.setQos(qos);
        }
        return message;
    }

    public void publish0(MqttClient client, String topic, MqttMessage message) {
        if (Objects.isNull(client)) {
            throw new RuntimeException("MqttApiService publish client must not be null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("MqttApiService publish topic must");
        }
        if (Objects.isNull(message)) {
            throw new RuntimeException("MqttApiServicemessage must not be null");
        }
        if (message.getPayload().length == 0) {
            throw new RuntimeException("MqttApiServicepublic message is empty");
        }
        try {
            client.publish(topic, message);
            //这里不用关闭这个客户端和连接
            // 关闭连接
            //client.disconnect();
            // 关闭客户端
            //client.close();
        } catch (Exception e) {
            log.error("MqttApiService publish error:{}", e.getMessage());
        }
    }


    public void subscribe0(MqttClient client, String topic, Integer qos, MqttCallback mqttCallback) {
        if (Objects.isNull(client)) {
            throw new RuntimeException("MqttApiService publish client is null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("MqttApiService publish topic is empty");
        }
        if (Objects.isNull(qos)) {
            qos = 1;
        }
        try {
            // 设置回调
            client.setCallback(mqttCallback);
            client.subscribe(topic, qos);
        } catch (Exception e) {
            log.error("MqttApiService subscribe0 error:{}", e.getMessage());
        }
    }

    /**
     * 发布
     *
     * @param clientId
     * @param content
     */
    public void publish(String clientId, String content) {
        if (StringUtils.isBlank(clientId)) {
            throw new RuntimeException("MqttApiService publish clientId is empty");
        }
        if (StringUtils.isBlank(content)) {
            throw new RuntimeException("MqttApiService publish content is empty");
        }
        MqttClient mqttClient = this.getMqttClient(clientId);
        MqttProperties mqttProperties = this.getMqttProperties(clientId);
        MqttMessage message = this.createMessage(mqttProperties.getTopic(), mqttProperties.getQos(), content);
        this.publish0(mqttClient, mqttProperties.getTopic(), message);
    }


    public void subscribe(String clientId, MqttCallback mqttCallback) {
        if (StringUtils.isBlank(clientId)) {
            throw new RuntimeException("MqttApiService subscribe clientId is empty");
        }
        MqttClient mqttClient = this.getMqttClient(clientId);
        MqttProperties mqttProperties = this.getMqttProperties(clientId);
        this.subscribe0(mqttClient, mqttProperties.getTopic(), mqttProperties.getQos(), mqttCallback);
    }

    public void subscribe2(String clientId, IMqttMessageListener messageListener) {
        if (StringUtils.isBlank(clientId)) {
            throw new RuntimeException("MqttApiService subscribe2 clientId is empty");
        }
        MqttClient mqttClient = this.getMqttClient(clientId);
        MqttProperties mqttProperties = this.getMqttProperties(clientId);
        try {
            mqttClient.subscribe(mqttProperties.getTopic(), mqttProperties.getQos(), messageListener);
        } catch (Exception e) {
            log.error("MqttApiService ubscribe2 error:{}", e.getMessage());
        }
    }

    public void unsubscribe(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            throw new RuntimeException("MqttApiService unsubscribe clientId is empty");
        }
        MqttClient mqttClient = this.getMqttClient(clientId);
        MqttProperties mqttProperties = this.getMqttProperties(clientId);
        try {
            mqttClient.unsubscribe(mqttProperties.getTopic());
        } catch (Exception e) {
            log.error("MqttApiService unsubscribe error:{}", e.getMessage());
        }
    }

}
