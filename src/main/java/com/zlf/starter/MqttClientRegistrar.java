package com.zlf.starter;

import com.zlf.config.MqttConfig;
import com.zlf.config.MqttProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Objects;

/**
 * @author zlf
 */
@Slf4j
@Configuration
@ConditionalOnClass(MqttClient.class)
@EnableConfigurationProperties(MqttConfig.class)
public class MqttClientRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private MqttConfig mqttConfig;

    public static final String MQTT_OPS_PREFIX = "mqtt-ops-";

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        List<MqttProperties> mps = mqttConfig.getMps();
        if (CollectionUtils.isEmpty(mps)) {
            throw new RuntimeException("mqtt配置不为空,请检查配置!");
        }
        for (MqttProperties m : mps) {
            ConstructorArgumentValues cas = new ConstructorArgumentValues();
            if (CollectionUtils.isEmpty(m.getBrokers())) {
                throw new RuntimeException("MqttClient Broker must not be empty");
            }
            if (StringUtils.isBlank(m.getUserName())) {
                throw new RuntimeException("MqttClient userName must not be empty");
            }
            if (StringUtils.isBlank(m.getPassword())) {
                throw new RuntimeException("MqttClient Password must not be empty");
            }
            if (StringUtils.isBlank(m.getClientId())) {
                throw new RuntimeException("MqttClient ClientId must not be empty");
            }
            cas.addIndexedArgumentValue(0, m.getBrokers().get(0));
            cas.addIndexedArgumentValue(1, m.getClientId());
            cas.addIndexedArgumentValue(2, new MemoryPersistence());
            MutablePropertyValues values = new MutablePropertyValues();
            // 注册bean
            RootBeanDefinition clientBeanDefinition = new RootBeanDefinition(MqttClient.class, cas, values);
            beanDefinitionRegistry.registerBeanDefinition(m.getClientId(), clientBeanDefinition);

            MutablePropertyValues values2 = new MutablePropertyValues();
            values2.addPropertyValue("userName", m.getUserName());
            values2.addPropertyValue("password", m.getPassword().toCharArray());
            values2.addPropertyValue("automaticReconnect", true);
            RootBeanDefinition optionsBeanDefinition = new RootBeanDefinition(MqttConnectOptions.class, null, values2);
            beanDefinitionRegistry.registerBeanDefinition(MQTT_OPS_PREFIX + m.getClientId(), optionsBeanDefinition);
            //一下三个参数有默认值不用设置(按需设置)
            //options.setCleanSession();
            //options.setKeepAliveInterval();
            //options.setConnectionTimeout();
            // 设置 socket factory
            /*
            TLS/SSL 连接 (按需设置)
            String caFilePath = "/cacert.pem";
            String clientCrtFilePath = "/client.pem";
            String clientKeyFilePath = "/client.key";
            SSLSocketFactory socketFactory = getSocketFactory(caFilePath, clientCrtFilePath, clientKeyFilePath, "");
            options.setSocketFactory(socketFactory);
            */
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        // 通过Binder将environment中的值转成对象
        mqttConfig = Binder.get(environment).bind(getPropertiesPrefix(MqttConfig.class), MqttConfig.class).get();
    }

    private String getPropertiesPrefix(Class<?> tClass) {
        return Objects.requireNonNull(AnnotationUtils.getAnnotation(tClass, ConfigurationProperties.class)).prefix();
    }

}
