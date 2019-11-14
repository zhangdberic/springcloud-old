# feign测试服务

其提供了一个用于测试feign的服务，其会调用sc-sampleservice服务来测试feign的配置的正确性。



feign的理解是把服务的调用RPC化，在feign没出来之前只能通过RestTemplate基于http协议来调用，而feign的出现则可以基于接口(java)代码的方式来远程调用服务。基于代码的feign(java接口)提供了比服务调用协议(文档)更好的可理解性。服务提供者可以编写基于feigh的服务调用客户端，供服务调用者使用。

feign是在ribbon上面应用，其底层实现还是ribbon。理解为ribbon的java接口化。

## 使用Feign实现声明式Rest调用

pom.xml

```xml
		<!-- spring cloud feign -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-openfeign</artifactId>
		</dependency>
```

根据要调用的服务，创建一个Feign接口

```java
@FeignClient(name = "sc-sampleservice")
public interface SampleServiceFeignClient {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	User findById(@PathVariable("id") Long id);

}
```

调用这个feigh声明的接口

原理：spring会bean后置处理会增强(Proxy)这个接口，包装为一个TraceLoadBalancerFeignClient代理来调用服务。

```java
@RestController
public class FeignTestController {
	
	@Autowired
	private SampleServiceFeignClient sampleServiceFeignClient;
	
	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id) {
		return this.sampleServiceFeignClient.findById(id);
	}

}
```

启动application加入@EnableFeignClient源注释

```java
@SpringBootApplication
@EnableFeignClients
public class FeignTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeignTestApplication.class, args);
	}

}
```



## 测试feign

浏览器发送请求http://192.168.5.78:8005/user/1，来验证是否能正确的调用sc-sampleservice服务。





