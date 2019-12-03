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

## 测试feign的url

浏览器发送GET请求http://192.168.5.78:8005/user/1，来验证是否能正确的调用sc-sampleservice服务。

浏览器发送GET请求http://192.168.5.78:8005/user/1?sleep=xxx，来验证是否能正确的调用sc-sampleservice的延时服务，一般应用测试读超时等。

浏览器发送GET请求http://192.168.5.78:8005/users?num=xxx，来验证大批量返回数据，例如：验证响应压缩配置等。

PostMan发送POST请求http://192.168.5.78:8005/add_users，来验证大批量请求数据，例如：验证请求压缩配置。

请求数据格式为json如下：

```json
[
    {
        "id": 19,
        "username": "account1",
        "name": "张三",
        "age": 20,
        "balance": 100
    },
    {
        "id": 19,
        "username": "account1",
        "name": "张三",
        "age": 20,
        "balance": 100
    }

]
```

PostMan发送POST请求http://192.168.5.78:8005/，增加一个User请求，验证feign的post请求处理。

请求数据格式为json如下：

```json
    {
        "id": 19,
        "username": "account1",
        "name": "张三",
        "age": 20,
        "balance": 100
    }
```

PostMan发送Post请求 http://192.168.5.78:8005/uploadFile ，上传一个文件，来验证feign上下文件。

基于PostMan的form-data格式来发送请求，参数的名字为file，选择file(文件)格式参数，value选择一个文件来上传。

如果需要测试大文件上传则要修改sc-feign-test和sc-sampleservice的配置，例如：

```yml
  spring:
    multipart: 
      # 整个请求大小限制(1个请求可能包括多个上传文件)
      max-request-size: 20MB
      # 单个文件大小限制
      file-size-threshold: 10MB 
```





## feign自定义配置

例如：

```yml
feign: 
  client: 
    config: 
      # 定义全局的feign设置
      default: 
        connectTimeout: 1000
      # 定义调用某个服务的feign设置,
      sc-sampleservice: 
       # 配置连接超时时间(毫秒)
       connectTimeout: 5000
       # 读取超时时间(毫秒)
       readTimeout: 5000
```

feign.client.config.**default**.x，定义的全局feign配置。

feign.client.config.**sc-sampleservice**.x 定义的某个服务的feign配置。



### 测试feign的某个服务配置覆盖全局配置

```yml
feign: 
  client: 
    config: 
      # 全局配置
      default: 
        readTimeout: 4000
      # 定义调用某个服务的feign设置,
      sc-sampleservice: 
       # 读取超时时间(毫秒)
       readTimeout: 5000
```



浏览器发送http://192.168.5.78:8005/user/1?sleep=5001则抛出java.net.SocketTimeoutException: Read timed out异常，发送http://192.168.5.78:8005/user/1?sleep=4001不抛出异常，证明sc-sampleservice定制readTimeout配置覆盖了default的readTimeout配置。

如果去掉sc-sampleservice的readTImeout配置则http://192.168.5.78:8005/user/1?sleep=4001会抛出java.net.SocketTimeoutException: Read timed out异常，证明全局配置生效。

如果去掉整个feign配置，发送http://192.168.5.78:8005/user/1?sleep=1001抛出java.net.SocketTimeoutException: Read timed out异常，发送http://192.168.5.78:8005/user/1?sleep=900，正常返回。可以证明readTimeout默认值为1000。

### 设置feign使用apache httpclient

#### 开启feign客户端对响应内容解压缩

pom.xml加入如下配置，使用apache的http的client的依赖。

```xml
		<!-- feign use apache httpclient -->
		<dependency>
			<groupId>io.github.openfeign</groupId>
			<artifactId>feign-httpclient</artifactId>
		</dependency>
```

sc-ribbon-test-dev.yml中加入，使用httpclient的配置。

```yml
# feign自定义配置
feign: 
  httpclient: 
    # 使用apache httpclient
    enabled: true
```

默认请求下，feign的http客户端使用的jdk自带的，其有很多问题，例如：不能正确的对gzip处理，建议使用上面的配置，修改为使用Apache的httpclient。

如果需要监控apache httpclient的请求和响应日志输出，则可添加：

```yml
logging:
  level:
    root: INFO
    # 日志输出apache httpclient的请求和响应日志
    org.apache.http.wire: DEBUG
```



### 测试feign的requestInterceptors配置

```yml
# feign自定义配置
feign: 
  client: 
    config: 
      # 定义调用某个服务的feign设置,
      sc-sampleservice: 
       # 请求拦截器
       requestInterceptors: 
         - com.sc.feign.controller.FeignBasicAuthRequestInterceptor
```

```java
package com.sc.feign.controller;

import java.nio.charset.Charset;

import feign.auth.BasicAuthRequestInterceptor;

public class FeignBasicAuthRequestInterceptor extends BasicAuthRequestInterceptor {

	public FeignBasicAuthRequestInterceptor() {
		super("黑哥", "密码", Charset.forName("UTF-8"));
	}

}

```

测试feign调用sc-sampleservice服务时，是否调用了FeignBasicAuthRequestInterceptor过滤器，发送请求：http://192.168.5.31:8005/user/1，观察调试日志(可以把logging.level.root: DEBUG)，会看到调试日志字样：

The modified request equals GET http://192.168.5.78:8001/1?sleep=900 HTTP/1.1

Authorization: Basic 6buR5ZOlOuWvhueggQ==

这里的Authorization: Basic 6buR5ZOlOuWvhueggQ==，就是上面FeignBasicAuthRequestInterceptor添加到请求头中的。证明requestInterceptors配置起作用。

### 测试feign重试和ribbon重试的关系

feign使用ribbon的重试配置，如果关闭了ribbon的重试，则feign的重试也关闭了。

测试方法：配置关闭调用sc-sampleservice的ribbon重试(见下面的yml)，部署两个sc-sampleservice服务实例，然后基于浏览器发送请求http://192.168.5.31:8005/user/1到sc-feign-test服务，sc-feign-test服务负载均衡调用sc-sampleservice服务，突然关闭一个sc-sampleservice服务实例，请求如果落到关闭的服务实例上，则抛出异常java.net.ConnectException: Connection refused: connect，证明：feign的重试依赖于ribbon的重试，如果ribbon关闭了重试，feign重试也无效。

### 测试feign的readTimeout和ribbon的ReadTimeout的关系

feign的readTimeout的配置优先级大于ribbon的ReadTimeout配置，feign的readTimeout配置或覆盖ribbon的ReadTimeout配置。feign的readTimeout默认值为1000。



测试方法1(feign.readTime < ribbon.ReadTimeout)：

feign的readTimeout=4000,ribbon的ReadTimeout设置为5000，具体yml设置如下：

发送请求http://192.168.5.31:8005/user/1?sleep=4500，报错read time out异常。

发送请求http://192.168.5.31:8005/user/1?sleep=3500，正确返回结果。

证明：使用了feign的readTimeout设置。

```yml
# feign自定义配置
feign: 
  client: 
    config: 
      # 定义全局的feign设置
      default: 
        connectTimeout: 1000
        readTimeout: 3000
      # 定义调用某个服务的feign设置,
      sc-sampleservice: 
       # 配置连接超时时间(毫秒)
       connectTimeout: 5000
       # 读取超时时间(毫秒)
       readTimeout: 4000    

# 配置ribbon属性
# 1.配置某个服务的ribbon属性
sc-sampleservice:
  ribbon:
    restclient: 
      enabled: true  
    ReadTimeout: 5000
```

测试方法2(feign.readTime > ribbon.ReadTimeout)：

feign的readTimeout=5000,ribbon的ReadTimeout设置为4000，具体yml设置如下：

发送请求http://192.168.5.31:8005/user/1?sleep=4500，正确返回结果。

发送请求http://192.168.5.31:8005/user/1?sleep=5001，报错read time out异常。

```yml
# feign自定义配置
feign: 
  client: 
    config: 
      # 定义全局的feign设置
      default: 
        connectTimeout: 1000
        readTimeout: 3000
      # 定义调用某个服务的feign设置,
      sc-sampleservice: 
       # 配置连接超时时间(毫秒)
       connectTimeout: 5000
       # 读取超时时间(毫秒)
       readTimeout: 5000    

# 配置ribbon属性
# 1.配置某个服务的ribbon属性
sc-sampleservice:
  ribbon:
    restclient: 
      enabled: true  
    ReadTimeout: 4000
```

### 测试feign的压缩(compression)配置

feign压缩配置是客户端（服务调用者）压缩配置，其必须有服务器端（服务提供者）的压缩支持，如果服务器端不支持压缩是没有任何意义，默认情况下，服务器端和客户端压缩都是关闭的。

**压缩本身就是一个双刃剑，其需要在CPU和网络带宽之间做出抉择。带宽充足的情况下建议关闭压缩(默认)，带宽不足的情况下建议开启压缩。**

#### 开启服务器端响应输出压缩

尽管服务器端的压缩配置和feign没关系，但要是想正确的说明feign压缩，则需要先开启服务器端压缩。

服务器端（服务提供者）要求开启需要如下操作：

##### 修改配置：

```yml
server:
  port: 8000
  # 支持压缩
  compression:
    enabled: true
    mime-types:
    - text/xml
    - text/plain
    - application/xml
    - application/json
    min-response-size: 1024
```

server.compression.enabled=true，用来开启服务器端压缩，例如：tomcat的响应输出压缩。server.compression.mime-types，那些输出内容类型可以被压缩，因为只有文字内容压缩才有意义。

server.compression.min-response-size，只有这个响应长度(Content-Length)才能被压缩，因为小字节流压缩没有任何意义。

##### 加入基于Content-Length响应头输出的过滤器：

举例子说明：

```java
@GetMapping(value = "/users", produces = "application/json;charset=UTF-8")
List<User> findUsers(@RequestParam(value="num",required=false,defaultValue="10") int num)
```

这是一个列出User对象的RestController方法，其参数num指定了要列出的记录数。返回值类型List<User>因为是RestController，其**返回值会被序列化为json输出**。json内容是可以被正常序列化输出，**但其基于Transfer-Encoding: chunked方式输出，无Content-Length响应头。**无Content-Length响应头的支持，servlet容器(tomcat)无法判断输出字节数(min-response-size配置项是根据Content-Length来判断的)，因此不管输出字节数多少，即使输出1个字节，也就执行压缩输出(Content-Encoding: gzip)。这显然不合理，我们还需要加入一个自定义的过滤器来正确的输出Content-Length响应头。

```java
package com.sc.sampleservice.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.ContentCachingResponseWrapper;
/**
 * 如需要测试响应压缩则可以开启本类
 * @author zhangdb
 *
 */
@Configuration
public class UserControllerJavaConfig {

	@Bean
	@ConditionalOnProperty(value="server.response.content-length",matchIfMissing=false)
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new Filter() {
			@Override
			public void init(FilterConfig filterConfig) throws ServletException {
			}

			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
				ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
		        chain.doFilter(request, responseWrapper);
		        responseWrapper.copyBodyToResponse();
			}

			@Override
			public void destroy() {
			}
		});
		List<String> urls = new ArrayList<String>();
		urls.add("/*");
		filterRegistrationBean.setUrlPatterns(urls);
		return filterRegistrationBean;
	}

}

```

```yml
server:
  port: 8000
  # 支持压缩
  compression:
    enabled: true
    mime-types:
    - text/xml
    - text/plain
    - application/xml
    - application/json
    min-response-size: 1024
  response: 
    content-length: true
```

这里重点说明一下ContentCachingResponseWrapper响应对象包装类，其继承了HttpServletResponseWrapper对象，对HttpServletResponse对象进行包装，RestController响应输出数据先暂存到这个包装器的content属性内(FastByteArrayOutputStream content)，在最终输出的时候计算content字节数输出Content-Length响应头。

题外话：**FastByteArrayOutputStream**这里类实现的非常好，其基于LinkedList<byte[]> buffers来实现字节流缓冲，OutputStream.write(bytes[])每一次输出对应一条LinkedList<byte[]>的Node。 

**测试服务器端压缩配置是否正确**

http://192.168.5.78:8000/users?num=13，输出的字节为963，小于min-response-size的1024配置，则直接输出不压缩，如下响应头：

HTTP/1.1 200 
X-Application-Context: sc-sampleservice:dev:8000
Content-Type: application/json;charset=UTF-8
**Content-Length: 963**
Date: Mon, 18 Nov 2019 07:38:46 GMT

http://192.168.5.78:8000/users?num=15，输出字节数1.08K，大于min-response-size的1024配置，则基于压缩输出，如下响应头：

HTTP/1.1 200 
X-Application-Context: sc-sampleservice:dev:8000
Content-Type: application/json;charset=UTF-8
**Transfer-Encoding: chunked**
**Content-Encoding: gzip**
Vary: Accept-Encoding
Date: Mon, 18 Nov 2019 07:40:37 GMT



#### 开启feign客户端对响应内容解压缩

前提：必须使用feign.httplcient.enabled=true或者开启okhttp3，否则服务响应内容无法解压缩。feign请求的响应处理会抛出无法解析json的异常。见上面的feign的httpclient客户端配置文档。

配置如下，其只支持全局配置，不支持某个服务的压缩配置。

```yml
feign: 
  httpclient: 
    # 使用apache httpclient
    enabled: true
  # 压缩
  compression:
    response: 
      enabled: true
```

前提服务器端已经开启了压缩，浏览器发送请求：http://192.168.5.31:8005/users?num=15，到sc-feign-test服务，sc-feign-test调用sc-sampleservice服务，观察sc-feign-test的输出日志输出有，Transfer-Encoding: chunked和Content-Encoding: gzip字样，说明服务器端是基于gzip响应输出，如果浏览器可以正确的显示内容，则说明解压缩正确。

浏览器再发送请求：http://192.168.5.31:8005/users?num=10，到sc-feign-test服务，sc-feign-test调用sc-sampleservice服务，观察sc-feign-test的输出日志输出有，Content-Length: 731字样，因为服务器端响应的内容长度小于服务器端server.compression.min-response-size=1024的配置，因此不压缩，原文返回。

注意：必须使用feign.httplcient.enabled=true或者开启okhttp3，否则服务响应内容无法解压缩。feign请求的响应处理会抛出无法解析json的异常。



#### 开启feign客户端请求压缩

**feign的请求压缩就是一个鸡肋，bug多而且太费事了，并且很多服务器端不支持请求的解压缩(建议不用)。**

配置如下，其只支持全局配置，不支持某个服务的压缩配置。

```yml
feign: 
  httpclient: 
    # 使用apache httpclient
    enabled: true
  # 压缩
  compression:
    request:
      enalbed: true
      mime-types:
      - text/xml
      - text/plain
      - application/xml
      - application/json;charset=UTF-8
      min-request-size: 1024
  client: 
    config:      
      default:
        # 请求拦截器
        requestInterceptors: 
          - com.sc.feign.controller.GzipRequestInterceptor   
```

feign.httpclient.enabled=true，设置使用apache http client，这个是必须的，否则无法正确的解压缩服务端返回的gzip响应流。

因为feign就是客户端，因此feign的压缩配置主要集中在request的压缩。

feign.compression.request.enabled=true，开启请求压缩。

feign.compression.request.mime-types，指定了开启压缩的请求内容类型。

feign.compression.request.min-request-size=1024，指定了大于1024个字节才开启压缩。

feign不会对请求内容进行压缩处理，只是增加了请求头**Transfer-Encoding: chunked**、**Content-Encoding: gzip**，没有对请求内容进行gzip处理，需要自己实现RequestInterceptor对象内容进行gzip压缩处理，如下代码：

```java
public class GzipRequestInterceptor implements   RequestInterceptor {
	
	private final Logger logger = LoggerFactory.getLogger(GzipRequestInterceptor.class);



	@Override
	public void apply(RequestTemplate template) {
		Map<String, Collection<String>> headers = template.headers();
		if (headers.containsKey(HttpEncoding.CONTENT_ENCODING_HEADER)) {
			Collection<String> values = headers.get(HttpEncoding.CONTENT_ENCODING_HEADER);
			if(values.contains(HttpEncoding.GZIP_ENCODING)) {
				logger.info("request gzip wrapper.");
				ByteArrayOutputStream gzipedBody = new ByteArrayOutputStream();
				try {
					GZIPOutputStream gzip = new GZIPOutputStream(gzipedBody);
					gzip.write(template.body());
					gzip.flush();
					gzip.close();
					template.body(gzipedBody.toByteArray(), Charset.defaultCharset());
				}catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}



}
```



上面的配置能保证，feign客户端请求的压缩发送到服务器端，但服务器端还要能正确的先解压缩请求。目前测试tomcat原生是不支持，nginx网上有插件可以支持，但没有测试过。

有人在github上写一个解压缩请求的代码，可以参照：

 https://github.com/ohmage/server/blob/master/src/org/ohmage/jee/filter/GzipFilter.java 

