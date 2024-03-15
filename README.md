# mqtt-spring-boot-start

mqtt启动器

1. 项目中引入依赖如下：<br>
```
        <dependency>
             <groupId>org.zlf</groupId>
             <artifactId>mqtt-spring-boot-start</artifactId>
              <version>1.0-SNAPSHOT</version>
        </dependency>
```        
2. nacos配置如下：<br>
```
mqtt:
  mps:
    # 多个brokers用,分割即可：如：xxx1,xxx2,,,下面两个配置的是同一个topic,所以往这个topic上发送消息,这个两个client会收到消息,订阅会打收到两条一样的消息日志，生产一般设置不同的topic
    - brokers: tcp://192.168.40.47:1883
      userName: zlf1
      password: xxx1
      clientId: zlf1_publish
      topic: mqtt/test
      qos: 2
      type: publish
    - brokers: tcp://192.168.40.47:1883
      userName: zlf2
      password: xxx2
      clientId: zlf2_subscribe
      topic: mqtt/test
      qos: 2
      type: subscribe
```
3. 启动类上加入如下注解：<br>
3.1 @EnableMqtt //开启Mqtt<br>

4.文章

https://blog.csdn.net/qq_34905631/article/details/135930755?spm=1001.2014.3001.5501

https://blog.csdn.net/qq_34905631/article/details/135919966?spm=1001.2014.3001.5501s
