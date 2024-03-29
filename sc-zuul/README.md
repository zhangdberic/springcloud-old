# spring cloud zuul

微服务网关：介于客户端和服务器端之间的中间层，所有的外部请求都会先经过微服务网关。微服务网关经过过滤出来和路由查询，转发请求到对应的服务器。

默认请求下zuul服务器，使用ribbon来定位eureka server中的微服务；同时，还整合了hystrix实现容错，所有经过zuul的请求都会在Hystrix命令中执行。

注意：尽管zuul起到了服务网关的作用，但还是强烈建议在生产环境中**zuul一定要前置nginx**。

![](./doc/zuul1.png)

## 1. zuul服务器配置

### 1.1 pom.xml

```xml
		<!-- spring cloud zuul -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
```

### 1.2 application.yml

```yaml
# 配置zuul->ribbon->使用APACHE HTTP Client
ribbon: 
  restclient:  
    enabled: true
# 配置zuul
zuul: 
  ignored-services: '*' 
  routes: 
    sc-sampleservice: /sampleservice/** 
```

配置zuul转发请求使用Apache HttpClient。

配置zuul忽略所有eureka上获取的服务，并指定某些服务对外开放。

这样是做的好处：

1. 解决安全问题，不能所有在eureka上的服务都暴露出去。

2. 通过routes配置可以指定服务的请求路径前缀和服务ID之间的映射(类似于DNS)，这样即使服务ID修改了，对外提供的URL不变。

3. 动态刷新路由配置(routes)，通过测试Edgware.SR6版本可以做到，git修改配置后，/bus刷新马上生效，无须重新启动zuul。

  POST请求，发送：http://config-ip:config-port/bus/refresh?destination=sc-zuul:** 

### 1.3 ZuulApplication.java

```java
@SpringBootApplication
@EnableZuulProxy
public class ZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZuulApplication.class, args);
	}

}
```

### 1.4 验证zuul启动是否成功

浏览器请求：http://zuul-ip:zuul-port/routes，查看返回的服务路由信息。

查看路由详细信息：http://192.168.5.31:8090/routes?format=details

查看应用的过滤器信息：http://192.168.5.31:8090/filters



## 2. zuul配置

### 2.1 配置使用路由前缀

```yaml
# 配置zuul
zuul: 
  # 忽略所有的服务
  ignored-services: '*' 
  # 指定请求前缀
  prefix: /api
  # 转发请求到服务时,是否去掉prefix前缀字符
  strip-prefix: true
  # 开放服务
  routes: 
    sc-sampleservice: /sampleservice/** 
```

关注：zuul.prefix=/api 和 zuul.strip-prefix=true 两处配置。

测试：http://192.168.5.31:8090/api/sampleservice/1，请求前缀加入了/api。

这样做还有一个好处，就是可以在zuul的前端加入nginx，nginx把所有的/api请求转发到zuul上。

**注意：zuul.routes的配置，支持/bus在线属性配置。**

### 2.2 敏感的Header设置

设置哪些Header可以穿透zuul传递到服务。

例如，设置zuul的sc-sampleservice服务路由，允许三个header请求头达到sc-sampleservice服务。

```yaml
# 配置zuul
zuul: 
  # 忽略所有的服务
  ignored-services: '*' 
  # 指定请求前缀
  prefix: /api
  # 转发请求到服务时,是否去掉prefix前缀字符
  strip-prefix: true
  # 配置路由
  routes: 
  	# 配置sc-sampleservice服务路由
    sc-sampleservice: 
      path: /sampleservice/** 
      sensitive-headers: Cookie,Set-Cookie,Authorization
```

关注：sensitive-headers: Cookie,Set-Cookie,Authorization

验证：查看zuul路由配置信息，http://192.168.5.31:8090/routes?format=details，返回：

![](./doc/zuul-sensitive-headers.png)

你也可以通过设置，zuul.ignoredHeaders 来忽略一些Header。

以上的配置支持/bus动态刷新配置。

### 2.3 Zuul上传文件

对于小于1M上传，无须任何任何处理，可以正常上传。大于1M，则需要特殊设置，配置允许最大请求字节数和单个上传的字节数。不支持/bus动态刷新配置。

```yaml
spring:   
  http:   
    multipart: 
      # 整个请求大小限制(1个请求可能包括多个上传文件)
      max-request-size: 20Mb
      # 单个文件大小限制
      max-file-size: 10Mb   
```

测试：postman发送post请求，http://192.168.5.31:8090/api/sampleservice/uploadFile

注意：mulitpart的设置，在zuul和upload服务都要设置。

如果是在互联网上传文件，则要考虑到网络带宽和延时等问题，因此要加大超时时间，例如：

```yaml
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 10000

ribbon:
  ReadTimeout: 10000
  ConnectTimeout: 2000
```

同上面mulitpart的设置，zuul和upload服务都要设置这个超时时间。

**考虑到上面的配置为了上传，加大了请求大小字节数和超时时间，这在上传操作很有用，但如果是普通的服务调用，则会有安全问题，因此强烈建议为upload单独设置一个zuul服务器，只有这台zuul服务器才需要调大这些配置。**例如：/api/dfss/upload的请求，nginx会根据url来转发到这台占用于上传处理的zuul上。

### 2.4 zuul过滤器

Zuul大部分功能都是通过过滤器来实现的。Zuul中定义了4中标准过滤器类型，这些过滤器类型对应请求的典型生命周期。

PRE：这种过滤器在请求被路由之前调用。可利用这种过滤器实现身份认证、获取请求的微服务、记录调试等。

POUTING：这种过滤器将请求路由到微服务，用于构建发送给微服务的请求。

POST：这种过滤器在路由到微服务以后执行，用于为响应添加Http Header、收集统计信息、将响应发送给客户端等。

ERROR：发送错误是执行该过滤器。

STATIC：不常用，直接在Zuul中生成响应，不将请求转发到后端微服务。

#### 2.4.1 内置过滤器

Zuul了一些过滤器，谁zuul启动。

**@EnableZuulServer所启动的过滤器**

PRE 类型过滤器：

ServletDetectionFilter：检查请求是否通过了Spring Dispatcher。

FormBodyWrapperFilter：解析表单数据，并为请求重新编码。目前效率低，如果基于json传递请求体，则可禁止该过滤器。

DebugFilter：调试过滤器，当设置zuul.debug.request=true，并且请求加上debug=true参数，就会开启调试过滤器。

ROUTE 类型过滤器：

SendForwardFilter：使用Servlet RequestDispathcer转发请求，转发位置存在在RequestContext的属性FilterConstant.FORWARD_TO_KEY中。用于zuul自身转发(forward)。

```yaml
zuul:
  routes:
    path: /path-a/**
    url: forward:/path-b
```

POST 类型过滤器：

SendResponseFilter：代理请求响应写入响应。

ERROR 类型过滤器：

SendErrorFilter：若RequestContext.getThrowable()不为null，则默认转发到/error，也可以使用error.path属性来修改。

**@EnableZuulProxy所启动过滤器**

@EnableZuulProxy启动的过滤包含上面@EnableZuulServer启动的过滤器。

PRE 类型过滤器：

PreDecorationFilter：根据RouteLocator对象确定要路由到的地址(微服务位置)，以及怎样去路由。

查看sc-zuul-swagger-test项目的DocumentationConfig类，了解RouteLocator对象如果被使用。

ROUTE 类型过滤器：

RibbonRouteFilter：使用Ribbon、Hystrix、HTTP客户端发送请求。servletId在RequestContext的属性FilterConstants.SERVICE_ID_KEY中。

SimpleHostRoutingFilter：如果路由配置直接指定了服务的url，而不能从eureka中获取位置，则使用这个过滤器。

**禁止某个过滤器**

zuul.<SimpleClassName>.<filterType>.disable=true

例如：zuul.FormBodyWrapperFilter.pre.disable=true

#### 2.4.2 自定义过滤器

因为声明了@Component定义为SpringBean，zuul会自动识别并应用这个过滤器。

```java
@Component
public class PreRequestLogFilter extends ZuulFilter {
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger(PreRequestLogFilter.class);

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		logger.info("send [{}] request to [{}].", request.getMethod(), request.getRequestURL().toString());
		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
	}

}
```

#### 2.4.3 伟大的RequestContext

zuul过滤器中最关键的技术就是RequestContext，其实一个上下文对象，包含zuul使用的几乎所有技术。下面说几个重点的：

1.其继承了ConcurrentHashMap对象，实现了Map所有的接口。

2.基于线程变量ThreadLocal来存储当前实例。

3.request和response都被存放到当前的map中，因此你可以在代码的任何位置来操作request和response。

4.setThrowable(Throwable th)代表zuul执行的过程中出现了异常，如果你的ZuulFilter在执行的过程中抛出了异常，zuul会自动调用这个方法添加异常对象到上下文中，你可以手工赋值（表示出现了异常）。你可以编写一个ErrorFilter来处理异常，这需要使用getThrowable()的方法来获取异常，如果异常处理完了，则一定要调用remove("throwable")来删除这个异常，表示已经没有异常了，否则异常会被传递下去。

5.任何响应的输出，不要直接使用response提供的方法来操作（RequestContext.currentContext().getResponse().xxx())，应该使用RequestContext提供的方法来设置response相关数据，例如：添加响应头RequestContext.currentContext().addZuulRequestHeader("code","ok");你调用任何RequestContext上的操作response的相关方法，SendResponseFilter过滤器(zuul原生)都会帮你输出。例如：setResponseBody(String)、setResponseDataStream(InputStream)、addZuulResponseHeader(String, String)、setOriginContentLength(Long)等。

6.setRouteHost(new URL("http:/xxx"))，这个类似于nginx的proxyPass ip地址，请求会被zuul转发到这个地址。

7.SERVICE_ID_KEY，zuul提供了一个关键字就是SERVICE_ID_KEY，设置这个值会改变请求的服务，例如：RequestContext.getCurrentContext().put(FilterConstants.SERVICE_ID_KEY, "myservices");，这里的服务可以是eureka上的服务、在配置文件zuul.route声明的手工服务等，你可以通过编程的方式来改变请求的服务。例如：手工指定服务，

```yaml
zuul:
  routes:
    tgms-service:
      path: /services
      serviceId: myservices
myservices:
  ribbon:
    NIWSServerListClassName: com.netflix.loadbalancer.ConfigurationBasedServerList
    listOfServers: 121.42.175.3:80,121.42.175.4:81       
```

8.REQUEST_URI_KEY，改变转发请求的uri，例如：RequestContext.getCurrentContext().put(FilterConstants.REQUEST_URI_KEY,"/tgms-services")，例如：你浏览器的请求地址为http://localhost:5000/services，则经过本代码转发到upstream的请求url已经是/tgms-services，不再是/services了。

#### 2.4.4 FilterConstants

zuul的关键字和内置过滤器执行顺序都在这个常量类中定义。看这个常量，你能有收获。

#### 2.4.5 ZuulFilter.filterOrder()

```java
	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}
    @Override
	public int filterOrder() {
		return 1;
	}
```

zuul根据这个值来决定过滤器执行的先后顺序，同一种filterType类型的两个ZuulFilter的filterOrder()不能相同，但不同种类filterType的filterOrder()可以相同，因为zuul是逐个类型(filterType)执行的。

技巧：因为PRE类型，可用的filterOrder()不多，一般情况下应使用2、3、4，这个可以通过查看FilterConstants常量来理解。如果你需要定义4个PRE类型的过滤器，filterOrder不够用了，这里有个技巧，你可以把两个没有相互依赖关系的ZuulFilter都定义为同一个filterOrder()，例如都定义为3。







### 2.4 Zuul容错和回退

#### 2.4.1 hystrix监控

http://zuul-ip:zuul-port/hystrix.stream，查看会查看到hystrix监控数据，也就是说默认情况下zuul的请求是收到zuul保护的，而且还能看出Thread Pools无相关数据，也证明了默认使用的hystrix隔离策略时SEMAPHORE。

#### 2.4.2 自定义回退类

因为声明了@Component定义为SpringBean，zuul会自动识别并应用这个回退提供者实现。

```java
@Component
public class MyFallbackProvider implements FallbackProvider {

	@Override
	public String getRoute() {
		// 表明为哪个微服务提供回退，* 表示所有微服务提供
		return "*";
	}

	@Override
	public ClientHttpResponse fallbackResponse(Throwable cause) {
		// 注意，只有hystrix异常才会好触发这个接口
		if (cause instanceof HystrixTimeoutException) {
			return response(HttpStatus.GATEWAY_TIMEOUT);
		} else {
			return this.fallbackResponse();
		}
	}

	@Override
	public ClientHttpResponse fallbackResponse() {
		return this.response(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ClientHttpResponse response(final HttpStatus status) {
		return new ClientHttpResponse() {

			@Override
			public InputStream getBody() throws IOException {
				return new ByteArrayInputStream(("{\"code\":\""+ status.value()+"\",\"message\":\"服务不可用，请求稍后重试。\"}").getBytes());
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				MediaType mt = new MediaType("application", "json", Charset.forName("UTF-8"));
				headers.setContentType(mt);
				return headers;
			}

			@Override
			public HttpStatus getStatusCode() throws IOException {
				return status;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return status.value();
			}

			@Override
			public String getStatusText() throws IOException {
				return status.getReasonPhrase();
			}

			@Override
			public void close() {
			}

		};
	}

}
```

测试验证：http://192.168.5.31:8090/api/sampleservice/1?sleep=2000，触发hystrix超时抛出，进而触发回退操作。



### 2.5 饥饿加载

zuul整合ribbon实现负载均衡，而ribbon默认是懒加载，可能会导致首次请求较慢。如果配置则修改为启动加载。

```yaml
zuul: 
  ribbon: 
    # 修改为启动加载(默认为懒加载)
    eager-load: 
      enabled: true
```

验证：启动时，查看log信息，会发现有DynamicServerListLoadBalancer字样。



### 2.6 QueryString 编码

如果要强制让query string与HttpServletRequest.getQueryString()保持一致，可使用如下配置：

```yaml
zuul: 
  # queryString保持一致
  forceOriginalQueryStringEncoding: true
```

注意：只对SimpleHostRoutingFilter有效。



### ~~2.7 Hystrix隔离策略和线程池~~

修改为线程隔离后，服务运行的线程池位置，两种模式只能选择一种：

1. 在同一个线程池RibbonCommand下运行，所有的服务都在这个RibbonCommand线程池要运行。
2. 每个服务都有一个独立的线程。

以上两个都有问题，第1种，如果所有的服务都在一个线程池下运行，那就失去了线程隔离的意义，一个服务出现阻塞，则整个RibbonCommand线程池瘫痪。第2种，如果调用100个服务，就分配100个线程池吗，这也有问题。

最理想，默认都使用RibbonCommand线程池调用服务，但可以为某个服务单独设置一个线程池。

#### 2.7.1 配置zuul使用thread隔离策略

默认情况下，Zuul的Hystrix隔离策略时**SEMAPHORE**。

可以使用zuul.ribbon-isolation-strategy=thread修改为THREAD隔离策略，修改后HystrixThreadPoolKey默认为RibbonCommand，这意味着，所有的路由HystrixCommand都会在相同的Hystrix线程池上执行。

修改后可以通过hystrix的dashborad观察，可以看到ThreadPools栏有数据了。

![](./doc/zull-thread-pool-default.png)

也可以为每个服务(路由)，使用独立的线程池，并使用hystrix.threadpool.服务名，来定制线程池大写：

```yaml
zuul:
  threadpool:
    useSeparateThreadPools: true
hystrix: 
  threadpool: 
    sc-sampleservice: 
      coreSize: 3    
```

![](./doc/zuul-useSeparateThreadPools.png)

### 2.8 设置超时时间

在基于zuul+hystrix+ribbon组合情况下设置读取超时时间(ReadTimeout)相对复杂一些，其需要先预估一个服务调用允许的超时时间，然后根据这个预估的参考值来计算相关属性值。

~~默认配置如下（配置文件中无超时相关配置）：~~

```properties
ribbon.restclient.enabled=false
hystrix.command.<ServiceId>.execution.isolation.thread.timeoutInMilliseconds=4000
<ServiceId>.ribbon.ConnectTimeout=1000
<ServiceId>.ribbon.ReadTimeout=1000
```

设置全局的超时时间，要同时设置如下几个值：

```properties
ribbon.restclient.enabled=true
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=xxxxx
ribbon.ConnectTimeout=xxxxx
ribbon.ReadTimeout=xxxxx
```

设置某个服务的超时时间，要同时设置如下几个值：

```properties
ribbon.restclient.enabled=true
hystrix.command.<ServiceId>.execution.isolation.thread.timeoutInMilliseconds=xxxxx
<ServiceId>.ribbon.ConnectTimeout=xxxxx
<ServiceId>.ribbon.ReadTimeout=xxxxx
```

#### 2.8.1 计算ribbonTimeout

**公式如下：**

```java
ribbonTimeout = (ribbonReadTimeout + ribbonConnectTimeout) * (maxAutoRetries + 1) * (maxAutoRetriesNextServer + 1);
```

**来源于：**org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommand.getRibbonTimeout()

例如：如下是你的配置；

```properties
ribbon.ConnectTimeout=1000
ribbon.ReadTimeout=10000
```

套用上面的公式计算（默认值：maxAutoRetries = 0，maxAutoRetriesNextServer = 1）：

(10000 + 1000) * (0 + 1) * (1 + 1) = 22000；

也就说，你配置的ReadTimeout为10000，而实际上系统计算出的ribbonTimeout为22000；因此应根据公式，反推算出一个ribbon.ReadTimeout；

计算ribbonTimeout的java代码：

```java
	protected static int getRibbonTimeout(IClientConfig config, String commandKey) {
		int ribbonTimeout;
		if (config == null) {
			ribbonTimeout = RibbonClientConfiguration.DEFAULT_READ_TIMEOUT + RibbonClientConfiguration.DEFAULT_CONNECT_TIMEOUT;
		} else {
			int ribbonReadTimeout = getTimeout(config, commandKey, "ReadTimeout",
				IClientConfigKey.Keys.ReadTimeout, RibbonClientConfiguration.DEFAULT_READ_TIMEOUT);
			int ribbonConnectTimeout = getTimeout(config, commandKey, "ConnectTimeout",
				IClientConfigKey.Keys.ConnectTimeout, RibbonClientConfiguration.DEFAULT_CONNECT_TIMEOUT);
			int maxAutoRetries = getTimeout(config, commandKey, "MaxAutoRetries",
				IClientConfigKey.Keys.MaxAutoRetries, DefaultClientConfigImpl.DEFAULT_MAX_AUTO_RETRIES);
			int maxAutoRetriesNextServer = getTimeout(config, commandKey, "MaxAutoRetriesNextServer",
				IClientConfigKey.Keys.MaxAutoRetriesNextServer, DefaultClientConfigImpl.DEFAULT_MAX_AUTO_RETRIES_NEXT_SERVER);
			ribbonTimeout = (ribbonReadTimeout + ribbonConnectTimeout) * (maxAutoRetries + 1) * (maxAutoRetriesNextServer + 1);
		}
		return ribbonTimeout;
	}
```

#### 2.8.2 计算hystrixTimeout

如果没有在配置文件中设置timeoutInMilliseconds，则使用ribbonTimeout作为hystrixTimeout，否则使用配置文件中的timeoutInMilliseconds作为hystrixTimeout。

**来源于：**

org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommand.getHystrixTimeout()

例如：如下是你的配置；

```yaml
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 22000
```

计算hystrixTimeout的java代码：

```java
	protected static int getHystrixTimeout(IClientConfig config, String commandKey) {
		int ribbonTimeout = getRibbonTimeout(config, commandKey);
		DynamicPropertyFactory dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
		int defaultHystrixTimeout = dynamicPropertyFactory.getIntProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds",
			0).get();
		int commandHystrixTimeout = dynamicPropertyFactory.getIntProperty("hystrix.command." + commandKey + ".execution.isolation.thread.timeoutInMilliseconds",
			0).get();
		int hystrixTimeout;
		if(commandHystrixTimeout > 0) {
			hystrixTimeout = commandHystrixTimeout;
		}
		else if(defaultHystrixTimeout > 0) {
			hystrixTimeout = defaultHystrixTimeout;
		} else {
			hystrixTimeout = ribbonTimeout;
		}
		if(hystrixTimeout < ribbonTimeout) {
			LOGGER.warn("The Hystrix timeout of " + hystrixTimeout + "ms for the command " + commandKey +
				" is set lower than the combination of the Ribbon read and connect timeout, " + ribbonTimeout + "ms.");
		}
		return hystrixTimeout;
	}
```

#### 2.8.3 ribbonTimeout和hystrixTimeout的关系

ribbonTImeout用于ribbon底层http client的socket的timeout，也就说用于网络的读取超时。

hystrixTimeout用于方法执行时间超时，理解为：future.get(hystrixTimeout)。

两者之间是在功能上是有区别。

例如：

ribbonTimeout超时报错：Caused by: java.net.SocketTimeoutException: Read timed out

hystrixTimeout超时报错：Caused by: com.netflix.hystrix.exception.HystrixRuntimeException: dfss-upload timed-out and no fallback available.



### 2.9 zuul使用ribbon重试

测试重试，后台开启两个sc-sampleservice的docker，使用zuul做为服务网关，接收请求，正常情况下是负载均衡分发，当停止一个sc-sampleservice的docker，再发送请求到zuul看能否正常返回结果，并通过日志查看是否有重试操作。

默认情况：就已经开启了重试，重试的默认值：maxAutoRetries = 0，maxAutoRetriesNextServer = 1，测试通过。

maxAutoRetries 同一实例重试次数，默认为0。

maxAutoRetriesNextServer 重试其它实例的最大次数，如果有3个实例，应该设置2，默认值1。



### 2.10 设置信号量

在默认的SEMAPHORE隔离策略下，信号量可以控制服务允许的并发访问量。

```yaml
zuul: 
  # 设置默认最大信号量
  semaphore: 
    max-semaphores: 100 
  # 设置某个服务的最大信号量
  eureka: 
    sc-sampleservice: 
      semaphore:
        max-semaphores: 50 
```

### 2.11 tomcat参数设置

通过设置tomcat参数来调整zuul对外服务能力

```yaml
server:  
  tomcat: 
    max-connections: 1000
    max-threads: 200
    min-spare-threads: 10
    accept-count: 50
```



### 3. Zuul高可用

Zuul可以像其它的spring cloud组件一样，把其注册到eureka上来实现zuul的高可用。但有些情况下，需要浏览器和app直接访问zuul，这种情况下可以使用nginx、HAProxy、F5等实现HA，并后接多个ZUUL来实现负载均衡和高可用。最佳实践是两种都用，两个zuul都注册到eureka上，供内网eureka客户端调用，并前置nginx(HA)供外网用户访问。



### 4.zuul整合其他非eureka上的服务

#### 4.1 配置routes路由请求到指定的URL

```yaml
zuul: 
  # 开放服务
  routes: 
    # 测试整合其它非eureka上的服务  
    dongyuit:
      path: /dongyuit/**
      url: http://www.dongyuit.cn/    
```

自定义了一个路由dongyuit，所有对/dongyuit/**前置的请求都会转发到http://www.dongyuit.cn/ ，例如：

http://192.168.5.31:8090/api/dongyuit/index.html

但要注意：上面的整合方法，请求不支持ribbon和hystrix，也就是说不支持负载均衡和hystrix容错。待以后解决。

#### 4.2 sidecar

需要被整合的服务端实现/health，这在整合一些第三方服务的情况下不可能，第三方法不可能给你实现一个/health功能。待以后解决。









## FAQ

### 1.安全问题

actutor安全问题？，待以后oauth2解决。







