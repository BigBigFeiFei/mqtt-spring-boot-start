package com.zlf.starter;

import com.zlf.util.MqttSpringUtils;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用需要在启动类上加入@EnableMqtt注解
 * 和 @Import(value = {MqttApplicationAware.class, MqttApiService.class,MqttSpringUtils.class})
 *
 * @author zlf
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({MqttClientRegistrar.class, MqttApplicationAware.class, MqttApiService.class, MqttSpringUtils.class})
public @interface EnableMqtt {

}
