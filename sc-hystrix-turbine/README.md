# hystrix+rabbitmq+turbine构建可视化监控

## 1.创建服务端(rabbitmq+turbine)

各个微服务发送hystrix的数据到rabbitmq（数据生产者），turbine订阅rabbitmq上的hystrix数据（数据消费者），然后通过Dashboard展示。

### 1.1 pom.xml

```xml
		<!-- spring cloud hystrix stream -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-turbine-stream</artifactId>
		</dependency>
		<!-- spring cloud stream rabbitmq -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-stream-rabbit</artifactId>
		</dependency>
```

### 1.2 application.yml

```yaml
spring:
  # 和spring-cloud-starter-bus-amqp配合,用于/bus/refresh分布式服务属性刷新
  # turbine hystrix.stream数据订阅
  rabbitmq:
    host: 192.168.5.29
    port: 5672
    username: admin
    password: Rabbitmq-401  
```

加入turbine连接到rabbitmq的配置。

### 1.3 TurbineServerApplication.java

```java
@EnableTurbineStream

```

