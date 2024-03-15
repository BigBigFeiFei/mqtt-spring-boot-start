package com.zlf.event;

import lombok.Getter;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.ApplicationEvent;

/**
 * @author zlf
 */
@Getter
public class MessageArrivedEvent extends ApplicationEvent {

    private static final long serialVersionUID = -6971129300095320086L;

    private String topic;

    private MqttMessage message;

    public MessageArrivedEvent(Object source, String topic, MqttMessage message) {
        super(source);
        this.topic = topic;
        this.message = message;
    }

}
