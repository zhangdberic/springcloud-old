# oauth2服务器端

## 1.oauth2的基本理论

## 2.oauth2配置

当前的代码例子，实现了一个基于jdbc存储client、user、authority静态数据，基于redis存放token。

一般情况下，当前的示例代码基本就够用了。

### 2.1 pom.xml

```xml
		<!-- spring cloud security oauth2 -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.security.oauth</groupId>
			<artifactId>spring-security-oauth2</artifactId>
		</dependency>	
```



