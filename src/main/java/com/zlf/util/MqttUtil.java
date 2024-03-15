package com.zlf.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可以搞成一个start启动器,根据配置类来实例化一个MqttClient注入一个bean,让spring来管理这个对象
 * 方法的参数太长,可以搞成一个配置类或者使用一个类的对象封装承接下的，
 * 让代码更具有封装性,
 * 可不可以先订阅后去发布,答案是可以
 * 这是一个工具类,可以单独使用
 */
@Slf4j
public class MqttUtil {

    /**
     * 项目启动就要将client建立连接和订阅建立好,
     * 然后项目中用的时候才不会由于第一次建立连接和第一次发布消息,未建立订阅而导致第一次发布的消息丢失
     * 这个问题可以先订阅后发布就可以避免这个问题了
     */
    private static final ConcurrentHashMap<String, MqttClient> clientMap = new ConcurrentHashMap<>();

    public static MqttClient createMqttClient(String broker, String userName, String password, String clientId) {
        if (StringUtils.isBlank(broker)) {
            throw new RuntimeException("createMqttClient Broker must not be empty");
        }
        if (StringUtils.isBlank(userName)) {
            throw new RuntimeException("createMqttClient userName must not be empty");
        }
        if (StringUtils.isBlank(password)) {
            throw new RuntimeException("createMqttClient Password must not be empty");
        }
        if (StringUtils.isBlank(clientId)) {
            throw new RuntimeException("createMqttClient ClientId must not be empty");
        }
        if (clientMap.containsKey(clientId)) {
            return clientMap.get(clientId);
        }
        MqttClient client = null;
        try {
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(userName);
            options.setPassword(password.toCharArray());
            //一下三个参数有默认值不用设置
            //options.setCleanSession();
            //options.setKeepAliveInterval();
            //options.setConnectionTimeout();
            // 设置 socket factory
            /*
            TLS/SSL 连接
            String caFilePath = "/cacert.pem";
            String clientCrtFilePath = "/client.pem";
            String clientKeyFilePath = "/client.key";
            SSLSocketFactory socketFactory = getSocketFactory(caFilePath, clientCrtFilePath, clientKeyFilePath, "");
            options.setSocketFactory(socketFactory);
            */
            options.setAutomaticReconnect(true);
            client.connect(options);
            clientMap.put(clientId, client);
        } catch (Exception e) {
            log.error("创建MqttClient异常:{}", e.getMessage());
        }
        return client;
    }

    public static MqttClient createMqttClient2(List<String> brokers, String userName, String password, String clientId) {
        if (CollectionUtils.isEmpty(brokers)) {
            throw new RuntimeException("createMqttClient2 Broker must not be empty");
        }
        if (StringUtils.isBlank(userName)) {
            throw new RuntimeException("createMqttClient2 userName must not be empty");
        }
        if (StringUtils.isBlank(password)) {
            throw new RuntimeException("createMqttClient2 Password must not be empty");
        }
        if (StringUtils.isBlank(clientId)) {
            throw new RuntimeException("createMqttClient2 ClientId must not be empty");
        }
        if (clientMap.containsKey(clientId)) {
            return clientMap.get(clientId);
        }
        MqttClient client = null;
        try {
            client = new MqttClient(brokers.get(0), clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(userName);
            options.setPassword(password.toCharArray());
            //以下三个参数有默认值不用设置
            //options.setCleanSession();
            //options.setKeepAliveInterval();
            //options.setConnectionTimeout();
            // 设置 socket factory
            /*
            TLS/SSL 连接
            String caFilePath = "/cacert.pem";
            String clientCrtFilePath = "/client.pem";
            String clientKeyFilePath = "/client.key";
            SSLSocketFactory socketFactory = getSocketFactory(caFilePath, clientCrtFilePath, clientKeyFilePath, "");
            options.setSocketFactory(socketFactory);
            */
            options.setAutomaticReconnect(true);
            options.setServerURIs(brokers.toArray(new String[brokers.size()]));
            client.connect(options);
            clientMap.put(clientId, client);
        } catch (Exception e) {
            log.error("创建MqttClient异常:{}", e.getMessage());
        }
        return client;
    }

    public static MqttMessage createMessage(String topic, Integer qos, String content) {
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

    public static void publish(MqttClient client, String topic, MqttMessage message) {
        if (Objects.isNull(client)) {
            throw new RuntimeException("publish client must not be null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("publish topic must");
        }
        if (Objects.isNull(message)) {
            throw new RuntimeException("message must not be null");
        }
        if (message.getPayload().length == 0) {
            throw new RuntimeException("public message is empty");
        }
        try {
            client.publish(topic, message);
            //这里不用关闭这个客户端和连接
            // 关闭连接
            //client.disconnect();
            // 关闭客户端
            //client.close();
        } catch (Exception e) {
            log.error("publish error:{}", e.getMessage());
        }
    }

    public static void subscribe(MqttClient client, String topic, Integer qos) {
        if (Objects.isNull(client)) {
            throw new RuntimeException("publish client is null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("publish topic is empty");
        }
        if (Objects.isNull(qos)) {
            qos = 1;
        }
        try {
            // 设置回调
            client.setCallback(new MqttCallback() {

                public void connectionLost(Throwable cause) {
                    log.error("connectionLost:{}", cause.getMessage());
                }

                public void messageArrived(String topic, MqttMessage message) {
                    String msg = new String(message.getPayload());
                    int qos1 = message.getQos();
                    log.info("subscribe topic:{}", topic);
                    log.info("subscribe Qos:{}", qos1);
                    log.info("subscribe msg:{}", msg);
                    //新增业务拓展接口对接
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("delivery complete:{}", token.isComplete());
                }

            });
            client.subscribe(topic, qos);
        } catch (Exception e) {
            log.error("subscribe error:{}", e.getMessage());
        }
    }

    public static void subscribe2(MqttClient client, String topic, Integer qos, MqttCallback mqttCallback) {
        if (Objects.isNull(client)) {
            throw new RuntimeException("publish client is null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("publish topic is empty");
        }
        if (Objects.isNull(qos)) {
            qos = 1;
        }
        try {
            // 设置回调
            client.setCallback(mqttCallback);
            client.subscribe(topic, qos);
        } catch (Exception e) {
            log.error("subscribe error:{}", e.getMessage());
        }
    }

    public static void publish2(String broker, String username, String password, String clientId, String topic, String content, Integer qos) {
        MqttClient mqttClient = MqttUtil.createMqttClient(broker, username, password, clientId);
        MqttMessage message = MqttUtil.createMessage(topic, qos, content);
        MqttUtil.publish(mqttClient, topic, message);
    }

    public static void subscribe3(String broker, String username, String password, String clientId, String topic, Integer qos) {
        MqttClient mqttClient = MqttUtil.createMqttClient(broker, username, password, clientId);
        MqttUtil.subscribe(mqttClient, topic, qos);
    }

    public static void subscribe4(String broker, String username, String password, String clientId, String topic, Integer qos, MqttCallback mqttCallback) {
        MqttClient mqttClient = MqttUtil.createMqttClient(broker, username, password, clientId);
        MqttUtil.subscribe2(mqttClient, topic, qos, mqttCallback);
    }

    public static void subscribe5(MqttClient client, String topic, Integer qos, IMqttMessageListener messageListener) {
        if (Objects.isNull(client)) {
            throw new RuntimeException("publish5 client is null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("publish5 topic is empty");
        }
        if (Objects.isNull(qos)) {
            qos = 1;
        }
        try {
            client.subscribe(topic, qos, messageListener);
        } catch (Exception e) {
            log.error("subscribe5 error:{}", e.getMessage());
        }
    }

    public static void subscribe6(String broker, String username, String password, String clientId, String topic, Integer qos, IMqttMessageListener messageListener) {
        MqttClient mqttClient = MqttUtil.createMqttClient(broker, username, password, clientId);
        MqttUtil.subscribe5(mqttClient, topic, qos, messageListener);
    }

    public static void unsubscribe(MqttClient mqttClient, String topic) {
        if (Objects.isNull(mqttClient)) {
            throw new RuntimeException("unsubscribe mqttClient is not null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("unsubscribe topic is not empty");
        }
        try {
            mqttClient.unsubscribe(topic);
        } catch (Exception e) {
            log.error("unsubscribe error:{}", e.getMessage());
        }
    }

}
