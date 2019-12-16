# 链路跟踪 sleuth

​		微服务跟踪(sleuth)其实是一个工具,它在整个分布式系统中能跟踪一个用户请求的过程(包括数据采集，数据传输，数据存储，数据分析，数据可视化)，捕获这些跟踪数据，就能构建微服务的整个调用链的视图，这是调试和监控微服务的关键工具。

## 1. sleuth配置

sleuth配置相对简单，只需要在pom.xml中加入启动依赖配置就可以了，Spring Boot启动会自动加载和配置sleuth。

### 1.1 pom.xml

```xml
		<!-- spring cloud sleuth -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-sleuth</artifactId>
		</dependency>
```

### 1.2 application.yml

sleuth对配置无要求。

### 1.3 Application.java

sleuth无须启动源注释

## 2.sleuth 测试