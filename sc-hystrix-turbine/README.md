# hystrix+rabbitmq+turbine+dashboard

# 构建可视化监控

hystrix数据监控，需要三部分：

1.turbine(mq模式)，其负责数据收集，其订阅了springCloudHystrixStream(exchanged)队列数据。

2.服务，把产生的hystrix监控数据发送给springCloudHystrixStream(exchanged)队列。

3.dashboard，负责可视化展示turbine收集的数据。

流程：

各个微服务发送hystrix的数据到rabbitmq（数据生产者），turbine订阅rabbitmq上的hystrix数据（数据消费者），然后通过Dashboard展示。



## 1.创建turbine(rabbitmq模式)

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

### 1.2 bootstrap.yml

```yaml
spring:
  application:
    name: sc-hystrix-turbine
  profiles:
    active: dev
```

### 1.3 application-dev.yml

```yaml
# 开发环境        
server: 
  port: 28031
spring:
  # java -D属性可以覆盖本远程参数
  cloud:
    config:
      overrideSystemProperties: false  
  rabbitmq:
    host: 192.168.5.29
    port: 5672
    username: admin
    password: Rabbitmq-401   
eureka:
  client:
    service-url:
      defaultZone: http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8070/eureka/,http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8071/eureka/
    # 客户端监控检查(定时检查并发送健康状态到eureka服务器)  
    healthcheck:
      enabled: true      
  instance:
    prefer-ip-address: true
    non-secure-port: 28031
management:
  port: 20011
  security:
    enabled: false    
```

**在application.yml中加入rabbitmq的连接信息**

注意：这里区别于其他spring cloud项目，其配置没有放到git上，不使用spring cloud config，那是因为Edgware的turbine(mq模式)存在bug，其在基于spring cloud config配置启动的时候报错：java.net.BindException: Address already in use: bind，因此使用本地application-dev.yml来存储配置信息。

而且还有注意：server.port一定要一个大的端口号，否则还报错，eureka.instance.non-secure-port配置要同server.port，而且management.port也要独立设置。

观察turbine的启动日志和eureka注册的turbine服务，会发现其端口是-1，这是正常，具体原因见下面：

```
在搭建springcloud微服务时，可以使用Turbine进行监控多个微服务，用dashboard展示数据。

不过在springboot1.5.x+springcloudEdgware版，使用消息中间间收集数据时会出现一个错误，导致Turbine整合rabbitmq项目无法运行。

错误信息：org.springframework.context.ApplicationContextException: Failed to start bean 'org.springframework.cloud.netflix.turbine.stream.TurbineStreamConfiguration'; nested exception is java.net.BindException: Address already in use: bind

导致原因：Turbine整合rabbitmq时会启动一个Netty容器，并将server.port 设为-1 ，从而关闭Servlet容器，就会导致yml文件中server.port无法正常工作。

解决办法：修改yml文件

server:
  port: 28031
spring:
  application:
    name: turbinservice
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.cloud.client.ipAddress}:28031
    non-secure-port: 28031
management:
  port: 20011
1.server.port尽量设置大一点，不然可能还是会报错
2.添加“non-secure-port:”该值与server.port的值一样
3.添加“management.port”该值设置一个随机数，尽量大一些，要与server.port的值不一样
该方法可能只对springboot1.5.x+springcloud Edgware版有效，可能其他版本不会有这个整合的问题。

在贴出我turbine+rabbitmq的pom文件

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-turbine-stream</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
        </dependency>
 
```

### 1.4 TurbineServerApplication.java

```java
@SpringBootApplication
@EnableTurbineStream
public class TurbineServerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TurbineServerApplication.class, args);
	}

}

```

### 1.5 验证是否正常启动

监控jvm的日志输出，例如：docker logs -f {id}，查看使用正常连接到rabbitmq，tomcat启动是否正常等。

通过健康监控检查URL： http://192.168.5.78:20011/health ，来确定启动是否成功。

通过rabbitmq的控制台，查看是否已经创建了springCloudHystrixStream的exchange。



### 1.6 Docker 启动脚本

docker run -itd --cap-add=SYS_PTRACE --name sc-hystrix-turbine --net host -e JAVA_OPTS="-Xms100m -Xmx100m -Xmn60m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev" dyit.com:5000/sc/sc-hystrix-turbine:1.0.1



## 2. 服务改造发送hystrix监控数据到rabbitmq

### 2.1 pom.xml

```xml
		<!-- spring cloud hystrix turbine client -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-stream-rabbit</artifactId>
		</dependency>
		
```

### 2.2 application.yml

```yaml
spring: 
  rabbitmq:
    host: 192.168.5.29
    port: 5672
    username: admin
    password: Rabbitmq-401    
```

**在application.yml中加入rabbitmq的连接信息**



## 3. dashboard可视化监控

具体可见：sc-hystrix-dashboard项目的README.md介绍。

监控turbine收集hystrix数据的URL：http://turbineip:port/turbine.stream