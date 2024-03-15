package com.zlf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author zlf
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

    private List<MqttProperties> mps;

}
