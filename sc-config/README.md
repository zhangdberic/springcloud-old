# 配置spring cloud config 配置中心

## 1. config 服务器端配置

### 1.1 pom.xml

```xml
	<dependencies>
		<!-- spring cloud config server -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-config-server</artifactId>
		</dependency>
		<!-- spring security 安全认证 -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>		
		<!-- spring cloud bus -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-bus-amqp</artifactId>
		</dependency>		
	</dependencies>
```

spring-boot-starter-security和spring-cloud-starter-bus-amqp不是必须的，spring-boot-starter-security是为保护config，客户端需要basic认证方式才能访问config，而spring-cloud-starter-bus-amqp是为了支持属性在线刷新。

### 1.2 bootstrap.yml

```yaml
spring:
  application:
    name: sc-config # 应用名称
  cloud:
    config:
      server:
        encrypt:
          enabled: false # 直接返回密文，而并非解密后的原文(需要客户端解密)
encrypt:
  key: it7CegZs-xxRfYpU # 配置文件加密秘钥
```

### 1.3 application.yml

```yaml
# 公有属性
# 指定默认的环境(默认开发环境)
spring:
  profiles:
    active: dev
# 开启安全认证    
security:
  basic:
    enabled: true
  user:
    name: sc-config
    password: veDSJeUX-JSxkWrk
         
# 开发环境    
---
server:
  port: 8080
spring:
  profiles: dev
  cloud:
    config:
      server:
        git:
          # Spring Cloud Config配置中心使用gitlab的话，要在仓库后面加后缀.git，而GitHub不需要
          uri: https://github.com/zhangdberic/config-repo.git
          search-paths: /**
          # 因为github的账户和密码不能泄露,因此需要在启动脚本中加入--spring.cloud.config.server.git.username=xxxx --spring.cloud.config.server.git.password=xxxx 
          username: zhangdb
          password: 12345678
  # 和spring-cloud-starter-bus-amqp配合,用于/bus/refresh分布式服务属性刷新.
  # 调用/bus/refresh,则刷新所有监听到本队列上的服务属性配置.
  # 调用/bus/refresh?destination={application}:**,则刷新监听到本队列上的某个服务(应用)属性配置.
  # 调用/bus/refresh?destination={application}:{instanceid},则只刷新监听到本队列上的某个服务(应用)的某个实例属性配置(用集群环境下的灰度发布).
  # 客户端为了支持/bus/refresh也需要在pom.xml中引入spring-cloud-starter-bus-amqp,并且在应用的git中(service-dev.yml)加上如下的队列配置.
  rabbitmq:
    host: 192.168.5.29
    port: 5672
    username: admin
    password: Rabbitmq-401             
```

### 1.4 ConfigServerApplication

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
```



## 2.config客户端配置

### 2.1 pom.xml

```xml
		<!-- spring cloud config client -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<!-- spring cloud bus -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-bus-amqp</artifactId>
		</dependency>
```

spring-cloud-starter-bus-amqp不是必须的，是为了支持属性在线刷新。

### 2.2 bootstrap.yml

```yaml
spring:
  application:
    name: sc-eureka
  profiles:
    active: dev
encrypt:
  key: it7CegZs-xxRfYpU  # 解密配置属性的秘钥(同配置服务器端秘钥)
# 开发环境        
---
spring:
  profiles: dev
  cloud:
    config:
      uri: http://192.168.5.78:8080
      profile: dev  # 指定从config server配置的git上拉取的文件(例如:sc-sample1service-dev.yml)
      username: sc-config   # config server的basic认证的user
      password: veDSJeUX-JSxkWrk # config server的basic认证的password    
```

注意：如果基于spring cloud config模式来定义服务的配置，那么就应该只创建bootstrap.yml，而application.yml应该放在config上，例如方在git上。而bootstrap.yml文件只应该包含上面例子中的内容：应用名、profile、config客户端配置等，不应在包含过多的配置，其它的配置都应该放在config的application.yml上。

依赖于spring boot的启动依赖和自动配置，spring boot项目启动后会自动在classpath上查找到config依赖包，然后根据bootstrap.yml上的config配置来，创建config相关的bean。



## 2. spring cloud config操作

### 2.1 获取某个服务的配置

**获取default属性：**

格式：http://config-ip:config-port/应用名.yml 

例如：http://192.168.5.78:8080/sc-eureka.yml

**获取dev环境属性**

格式：http://config-ip:config-port/应用名-dev.yml 

例如：http://192.168.5.78:8080/sc-eureka-dev.yml

### 2.2 bus属性刷新(/bus/refresh)

#### 2.2.1 pom.xml中加入spring-cloud-starter-bus-amqp

spring cloud config 服务器和客户端的pom.xml都要加入这个起步依赖，服务器端config运行时会在rabbitmq上创建springCloudBus的exchanged。客户端config运行会创建一个queue，基于这个springCloudBus的exchanged路由绑定。这样就形成了客户端订阅，服务器端发布的模式，只有你修改git上的属性，并发送请求 /bus/refresh?destination=服务名:** 到config服务器，config服务验证属性确实改变了，就会put修改属性通知到对列，config客户端马上就会接受到属性改变的通知，并重新配置。

#### 2.2.2 刷新属性所在类上加入@RefreshScope源注释

```java
@Component
@RefreshScope
public class ConfigProperties {
	
	@Value("${config.property.refreshProperty1}")
	private String refreshProperty1;

	public String getRefreshProperty1() {
		return refreshProperty1;
	}

	public void setRefreshProperty1(String refreshProperty1) {
		this.refreshProperty1 = refreshProperty1;
	}
}

```

强烈建立把属性都放到一个或多个配置类中，这样既方便管理，又方便刷新。

引用这个ConfigProperties的controller代码。

```java
@RestController
public class TestConfigController {

	@Autowired
	private ConfigProperties configProperties;

	@GetMapping("/getConfigProperties")
	public ConfigProperties getConfigProperties() {
		ConfigProperties configProperties = new ConfigProperties();
		configProperties.setRefreshProperty1(this.configProperties.getRefreshProperty1());
		return configProperties;
	}

}
```

#### 2.2.3 发送属性刷新请求

发送POST请求到，config服务器，请求URL：http://config-ip:config-port/bus/refresh?destination=服务名:**  

测试URL：http://192.168.5.78:8080/bus/refresh?destination=sc-config-test:**   ，POST请求，Basic认证的用户名和密码为： sc-config: veDSJeUX-JSxkWrk 。

可以是postMan来测试，也可以使用firefox的 RESTClient插件来测试。

#### 2.2.4 测试属性刷新

1.测试发送请求获取属性信息：http://192.168.5.31:8500/getConfigProperties，查看刷新前的属性值。

2.修改github上的config-repo/sc-hystrix-test/sc-hystrix-test-dev.yml配置文件内的属性：

```yaml
# 测试属性在线刷新    
config:
  property:
    refreshProperty1: 马应龙唇膏
```

3.发送POST请求，http://192.168.5.78:8080/bus/refresh?destination=sc-config-test:** ，Basic认证的用户名和密码为： sc-config: veDSJeUX-JSxkWrk ，触发属性刷新。

4.再发送请求获取属性信息：http://192.168.5.31:8500/getConfigProperties，查看刷新后的属性值。

### 2.3 属性加密

#### 2.3.1 配置对称加密KEY

config服务器端和config客户端的bootstrap.yml的配置文中都要加入，如下配置：

```yaml
encrypt:
  key: it7CegZs-xxRfYpU  # 解密配置属性的秘钥(同配置服务器端秘钥)
```

#### 2.3.2 使用config服务器提供的/encypt加密值

使用curl来发送加密请求，格式：

curl --user basic-username:basic-password http://config-ip:config-port/encrypt -d 加密属性值

例如：curl --user sc-config:veDSJeUX-JSxkWrk http://192.168.5.78:8080/encrypt -d 123

返回：ab1582e88160b08500ed785b666cd274d3343ecb60288b92c061dd049bc5e2a1

#### 2.3.3 修改application.yml属性为加密值

注意：加密的属性值和普通的属性值是有区别的，加密属性值格式：

'{cipher}加密后的属性值'，单引号是必须的，前缀字符串{cipher}也是必须的。

```yaml
config:
  property:
    encryptedProperty2: '{cipher}2e2674efca5bf456f034872fe890686814e0979474781fc2cee9054d305ca067'
```



## 2. 集群

spring cloud 配置集群，最头疼的就是，是把spring cloud config以服务的方式注册的eureka上来保证可靠性，还是把spring cloud config做成vip的HA。就好比是一个先有鸡还是先有蛋的问题。

### 第1种：

​	好处：简单，可靠性也能保证，config服务也注册到eureka上，由eureka统一管理。

​	缺点：eureka无法使用spring cloud config，application.yml要放在eureka项目本地文件系统上，如果配置改变需要到本地修改，然后重启eureka。

​                每个服务，需要写死eureka.client.service-url.defaultZone的地址，因为要先到eureka注册中心获取springcloudconfig服务的地址，然后才能调用config来获取服务配置。

​    整个spring cloud系统需要先启动eureka集群。

### 第2种：

​	好处：eureka项目的应用配置也可以加入到spring cloud config中，可以直接在git上查看和修改(配置统一管理)。

​	坏处：需要配置VIP，加入了复杂性。

​    整个spring cloud系统需要先启动spring clolud config集群。



但不管是第1种，还是第2种，gitlabs不能是单点，rabbitmq也不能但单点，也应该是双机(VIP)。

**目前我的选择是使用第2种：config vip方式。**

基于VIP的方案来构建集群，VIP后接两个spring cloud config的docker，VIP可以使用keepalived来实现，但这需要两台宿主机上安装和配置keeaplived。

服务配置客户端spring.cloud.config.uri: http://vip:port配置为vip地址。

gitlabs也基于HA方式，也可以安装到spring cloud config docker的宿主机上（这个具体没安装过，但应该没问题），gitlabs的数据基于drbd来保证可靠性。

## docker启动脚本

docker run -itd --cap-add=SYS_PTRACE --name sc-config1 --net host -e JAVA_OPTS="-Xms100m -Xmx100m -Xmn30m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev --spring.cloud.config.server.git.username=xxxx--spring.cloud.config.server.git.password=yyyy" dyit.com:5000/sc/sc-config:1.0.1

## 



















