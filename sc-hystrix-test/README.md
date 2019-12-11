# 使用hystrix实现微服务的容错处理

当依赖的服务不可用时，服务自身会不会被拖垮？这是我们要考虑的问题。

## 理论基础

断路器可理解为对容易导致错误的操作的代理。这种代理能够统计一段时间内调用失败的次数，并决定是正常请求依赖的服务还是直接返回。

断路器可以实现快速失败，如果它在一段时间内检测到许多类似的错误（例如超时），就会在之后的一段时间内，强迫对该服务的调用快速失败，既不再请求所依赖的服务。

断路器也可自动诊断依赖的服务是否已经恢复正常。如果发现依赖的服务已经恢复正常，那么就会恢复请求该服务。使用这种方式，就可以实现微服务”自我修复“，当依赖的服务不正常时，打开断路器的快速失败，从而防止雪崩效应；当发现依赖的服务恢复正常时，又会恢复请求。

## 断路器状态转换逻辑

正常情况下，断路器关闭，可以正常请求依赖的服务。

当一段时间内，请求失败率达到一定阀值（错误率达到50%，请求数大于20），断路器就会打开（跳闸）。此时，不会再去请求依赖的服务。

断路器打开一段时间后，会自动进入“半开”状态。此时，断路器可允许一个请求通过访问依赖的服务。如果请求能够调用成功，则关闭断路器；否则继续保持打开状态。

## hystrix提供了

1. 包裹请求：使用HystrixCommand，包裹服务的调用逻辑，每个命令在独立线程中执行。命令模式。

2. 跳闸机制：当前某个服务的错误率超出一定阀值时，Hystrix可以自动跳闸，停止请求该服务一段时间。

3. **资源隔离**：Hystrix为每个依赖都维护一个小型的线程池。如果该线程池已满，发往该依赖的请求就立即被拒绝，而不是排队等候，从而快速失败。

4. 监控：Hystrix提供实时的监控运行指标。

5. 回退机制：当前请求失败、超时、被拒绝或者短路器已经打开（跳闸），直接调用“回退方法”返回数据。

6. 自我修复：断路器打开一段时间后，会自动进入“半开”状态，允许一个请求通过，要验证服务是否可用，可用则关闭断路器，不可以用继续打开断路器。

   ### hystrix线程(Thread)隔离

   #### 在那个线程池中运行?

   基于thread模式，可以做到线程隔离，被hystrix保护的方法(@HystrixCommand)，运行在一个单独的线程池内，与调用线程分离。默认情况下根据**类名**来创建线程池，例如：UserController内部有两个方法，addUser(User)和ModifyUser(Long,User)方法，这个两个方法都使用@HystrixCommand修饰(被Hystrix保护)，那么Hystrix会根据类名UserController来创建一个线程池，这两个方法的调用运行在在UserController线程池中。默认：池的大小为10个，队列大小为5。这就有个问题，如果一个jvm上跑100个Controller，那么就会创建100个线程池，每个10个线程，那就是1000个线程，这显然不能接受。因此我们要定制那些服务接口(Hystrix保护的方法)，放在同一个线程池上，并合理的设置线程池和线程内线程的个数。

   通过使用@HystrixCommand源注释内的threadPoolKey属性，设置当前方法运行在那个线程池上。例如：不同Controller的内的方法，运行在一个线程池上，或者同一个Controller内的两个方法运行在不同的线程池上。
   
   ```java
   	@HystrixCommand(groupKey = "heigeGroup", threadPoolKey = "heigeThreadPoolKey", fallbackMethod = "findUser5ByIdFallback", threadPoolProperties = { 
   			@HystrixProperty(name = "coreSize", value = "1"),
   			@HystrixProperty(name = "maxQueueSize", value = "10") 
   			}
   	)
   	@GetMapping(value = "/user5/{id}")
   	public User findUser1ById(@PathVariable Long id, @RequestParam int sleep) {
   		logger.info("request param sleep[{}].", sleep);
   		try {
   			Thread.sleep(sleep);
   		} catch (InterruptedException ex) {
   			throw new RuntimeException(ex);
   		}
   		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
   	}
   	
   	@HystrixCommand(groupKey = "heigeGroup", threadPoolKey = "jiaojieThreadPoolKey", fallbackMethod = "findUser5ByIdFallback", threadPoolProperties = { 
   			@HystrixProperty(name = "coreSize", value = "2"),
   			@HystrixProperty(name = "maxQueueSize", value = "10") 
   			}
   	)
   	@GetMapping(value = "/user5a/{id}")
   	public User findUser2ById(@PathVariable Long id, @RequestParam int sleep) {
   		logger.info("request param sleep[{}].", sleep);
   		try {
   			Thread.sleep(sleep);
   		} catch (InterruptedException ex) {
   			throw new RuntimeException(ex);
   		}
   		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
   	}

   ```
   



#### hystrix线程池运行是否会阻塞调用的线程?

目前测试结果是，即使是使用线程池隔离策略（也就说hystrix保护的方法在独立的线程池上运行），调用这个hystrix保护方法的线程任会被阻塞，等待hystrix保护方法在其线程上运行后返回。

测试方法，tomcat设置1个线程，使用hystrix保护方法(method)，默认池大小为10个。

如果发送请求，则controller代码运行在tomcat的线程池上，hystrix保护的方法运行在hystrix线程池上。

在浏览器上发送请求，代码执行进入到hystrix保护的方法后休眠(sleep)，这时如果再使用另一个浏览器发送同样的请求，无法进入到controller，说明tomcat的这个线程已经被占用，tomcat执行线程在等待这个hystrix保护的方法线程执行完返回。

设置tomcat只最大运行的最大线程数为1：

```yaml
server:  
  tomcat: 
    max-threads: 1
    min-spare-threads: 1
```

加大读取超时时间配置60000：

```yaml
hystrix:
  command:
    default: 
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 60000    
ribbon:
  ReadTimeout: 60000            
```

具体的代码，参见HystrixTest6Controller类。

总结：经过上面的测试，得到结论，调用线程和hystrix保护方法运行线程是1对1的，也就是说执行一个操作需要阻塞两个线程。那你可能要有疑问，hystrix是为了保护程序而使用线程隔离，而现在却多了1倍的线程，这样有什么意义？意义就在于即使多使用了1倍的线程，而对整个系统进行了保护。例如：默认hystrix保护的方法运行在线程大小为10的线程池中，如果被保护的方法出现慢处理，则10个线程都在运行中，这时如果第11个请求调用hystrix保护方法，由于线程池已满则直接回退。如果没有hystrix线程隔离，则所有tomcat线程都会被占满，知道最后超出max-threads限制。我们尽管使用了20个线程来处理，总比出现问题后整个系统所有的服务调用雪崩强呀。



# 使用hystrix实现微服务的容错处理

**pom.xml**

```xml
		<!-- spring cloud hystrix -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
		</dependency>
		<!-- spring cloud feign -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-openfeign</artifactId>
		</dependency>	
		<!-- feign use apache httpclient -->
		<dependency>
			<groupId>io.github.openfeign</groupId>
			<artifactId>feign-httpclient</artifactId>
		</dependency>	
```

上面的spring cloud feign和feign-httpclient不是必须的，如果要结合feign+hystrix，则需要加入，否则如果你只使用ribbon则无必要加入。如果需要feign支持文件上传，则还有加入feign-form相关，具体见"feign文档"。

**启动application加入@EnableHystrix源注释**

```java
@SpringBootApplication
@EnableHystrix
@EnableFeignClients
public class HystrixTestApplication {
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(HystrixTestApplication.class, args);
	}

}
```

如果要结合feign+hystrix，则需要加入@EnableFeignClients

**hystrix保护方法**

```java
	@HystrixCommand(fallbackMethod = "findUser1ByIdFallback")
	@GetMapping(value = "/user1/{id}")
	public User findUser1ById(@PathVariable Long id, @RequestParam int sleep) {
		logger.info("request param sleep[{}].", sleep);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
	}
```

在需要hystrix保护的方法上，加上@HystrixCommand源注释，HystrixCommand的源注释参数比较多，具体见：com.netflix.hystrix.HystrixCommandProperties。



# 测试hystrix

## 测试超时

根据hystrix的默认配置，来测试超时。

**控制器代码**

为了方便测试，我们加入了sleep参数，可以控制方法的休眠时间，好方便测试延时。

```java
	/**
	 * 测试超时
	 * @param id
	 * @param sleep 休眠时间
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "findUser1ByIdFallback")
    // @HystrixCommand(fallbackMethod = "findUser1ByIdFallback",commandProperties= {@HystrixProperty(name="execution.isolation.strategy",value="SEMAPHORE")})
	@GetMapping(value = "/user1/{id}")
	public User findUser1ById(@PathVariable Long id, @RequestParam int sleep) {
		logger.info("request param sleep[{}].", sleep);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
	}

	/**
	 * 回退方法(降级方法)
	 * @param id
	 * @param sleep
	 * @return
	 */
	public User findUser1ByIdFallback(Long id, int sleep) {
		logger.info("into fallback[{}].");
		User user = new User();
		user.setId(-1l);
		user.setName("默认用户");
		return user;
	}	
```

**测试用例**

```java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HystrixTest1 extends HystrixTestBase {
	
	@Value("${local.server.port}")
	private int port;
	
	
	/**
	 * 测试方法：发送一个正确请求，休眠1100ms。
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void test() throws InterruptedException {
		// 发送一个正常请求，休眠500ms
		int sleep = 500;
		User user = this.sendSpeelRequest(sleep);
		logger.info("[{}]延时,返回正常User{}].",sleep,user);
		// 发送一个超时请求，休眠1001ms
		sleep = 1001;
		user = this.sendSpeelRequest(sleep);
		logger.info("[{}]延时,返回回退User{}].",sleep,user);

	}
	
	@Override
	int getPort() {
		return this.port;
	}

}
```

本测试关系到配置属性：

execution.isolation.thread.timeoutInMilliseconds=1000(默认值)

本测试还可以验证，在SEMAPHORE模式也支持超时判断。



### 测试hystrix的断路器打开（跳闸）、保护机制、回退机制、自我修复

根据hystrix的默认配置，来测试hystrix的断路器打开（跳闸）、保护机制、回退机制、自我修复。

这里只是对关键的代码说明，具体还有参照本项目的代码。

**控制器代码**

为了方便测试，我们加入了status参数，status=true视为成功请求，status=false视为失败请求（抛出异常），方便测试错误率等。

```java
	@HystrixCommand(fallbackMethod = "findUser2ByIdFallback")
	@GetMapping(value = "/user2/{id}")
	public User findUser2ById(@PathVariable Long id, @RequestParam Boolean status) {
		if (status) {
			logger.info("request param status[{}].", status);
			return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
		} else {
			int fc = this.failureCount.incrementAndGet();
			logger.info("request param status[{}], failure count[{}].", status, fc);
			throw new RuntimeException("test request failure.");
		}
	}

	/**
	 * 回退方法(降级方法)
	 * @param id
	 * @return
	 */
	public User findUser2ByIdFallback(Long id, Boolean status) {
		logger.info("into fallback[{}].",this.failureCount.get());
		User user = new User();
		user.setId(-1l);
		user.setName("默认用户");
		return user;
	}
```

**验证测试用例**

```java
/**
 * hystrix 测试用例2
 * 用于验证：进入短路器打开(跳闸)状态后，5s内正确的请求也会被回退， 超出5s后正确的请求会关闭短路器。
 * @author zhangdb
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HystrixTest2 extends HystrixTestBase {

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private HystrixHealthIndicator hystrixHealthIndicator;

	@Test
	public void test() throws InterruptedException {
		logger.info("init hystrix status[{}].", this.hystrixHealthIndicator.health().getStatus());

		// 记录错误窗口期开始时间
		long failureBeginTime = System.currentTimeMillis();
		// 发送一个失败请求，激活错误窗口期
		this.sendFailureRequest();

		// 触发短路器跳闸
		this.sendFailureRequest(20, 230);
		logger.info("[{}]毫秒内执行了21个失败请求,短路器状态[{}].", System.currentTimeMillis() - failureBeginTime, this.hystrixHealthIndicator.health().getStatus());

		// 5s内的正确请求，都会被回退
		for (int i = 0; i < 4; i++) {
			User user = this.sendSuccessRequest();
			this.logger.info("5s内的正确请求都会被回退，User:{}，短路器状态[{}].", user, this.hystrixHealthIndicator.health().getStatus());
			Thread.sleep(1000);
		}

		// 5s后的正确的请求，会正确返回，断路器关闭
		Thread.sleep(1000);
		User user = this.sendSuccessRequest();
		this.logger.info("超出5s后正确的请求正常执行，User:{}，短路器状态[{}].", user, this.hystrixHealthIndicator.health().getStatus());

	}
```

运行这个测试用例，查看日志输出，并且参照上面的理论文档介绍。

本测试关系到的配置属性：

metrics.rollingStats.timeInMilliseconds=10000 ，错误窗口时间，第一个错误请求开始。

circuitBreaker.requestVolumeThreshold=20，打开跳闸的最低请求数。错误时间窗内，只有大于这个值才能跳闸。

circuitBreaker.errorThresholdPercentage=50，失败请求的数大于这个百分比，才能跳闸。

例如：this.sendFailureRequest(20, 230);，发送20个错误请求，每个间隔230ms，这样5000-6000ms内就会打开断路器，正好满足上面三项配置要求。

circuitBreaker.sleepWindowInMilliseconds=5000，跳闸后这个配置时间内，任何请求（正确或者失败请求）都会被回退，这是一种保护。超出这个值，则允许一个请求通过，如果正确返回则关闭断路器，如果失败则保持跳闸状态，并且还是本配置时间内，都会被回退。



### 测试Hystrix的线程上下文变量(ThreadLocal)传递

如果hystrix基于的THREAD模式，则ThreadLocal中的值使用无法传递到@HystrixCommand声明的方法，因为隶属两个不同的线程。

有兴趣大家可以看：SleuthHystrixConcurrencyStrategy的实现代码。

如下代码，是一个很平常的代码，User对象被存放到UserContext中(基于ThreadLocal存放User)，但在其无法传递到hystrix保护的方法内，因为hystrix的保护方法执行在另一个线程内，和调用线程不是同一个线程，因此ThreadLocal无法传递。

```java
	@GetMapping(value = "/user3/{id}")
	public User findUser3ById(@PathVariable Long id) {
		User user =  this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
		UserContext.setUser(user); // UserContext内部基于ThreadLocal实现
		user = this.hystrixConcurrentStrategyTestBean.findUser3Hystrix(); // 被hystrix保护
		UserContext.remove();
		return user;
	}
```

```java
public class UserContext {
	
	private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();
	
	public static User getUser() {
		return userThreadLocal.get();
	}
	
	public static void setUser(User user) {
		userThreadLocal.set(user);
	}
	
	public static void remove() {
		userThreadLocal.remove();
	}

}
```

```java
@Component
public class HystrixConcurrentStrategyTestBean {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(HystrixConcurrentStrategyTestBean.class);
	
	
	@HystrixCommand(fallbackMethod = "findUser3HystrixFallback")
	public User findUser3Hystrix() {
		User user = UserContext.getUser(); // UserContext内部基于ThreadLocal实现
		logger.info("user value[{}].",user);
		return user;
	}
	
	public User findUser3HystrixFallback() {
		logger.info("into fallback.");
		User user = new User();
		user.setId(-1l);
		user.setName("默认用户");
		return user;
	}

}
```

为了解决上面的问题，我们要自定义hystrix的并发策略类（HystrixConcurrencyStrategy），继承这个类，并重新实现wrapCallable(Callable<T> callable)方法，对callback参数进行包装，并返回给hystrix线程。在call()方法调用前，把User对象传递到hystrix线程的线程变量(ThreadLocal)中。代码如下：

```java
public class UserContextCallable<V> implements Callable<V> {
	private final User user;
	private final Callable<V> callable;

	/**
	 * 外围线程初始化(例如:tomcat请求线程)
	 * @param callable
	 * @param user
	 */
	public UserContextCallable(Callable<V> callable,User user) {
		super();
		this.user = user;
		this.callable = callable;
	}

	/**
	 * Hystrix隔离仓线程调用(hystrix执行线程)
	 */
	@Override
	public V call() throws Exception {
		UserContext.setUser(this.user); // 调用前把User对象绑定到hystrix的线程变量
		try {
			V v = this.callable.call();
			return v;
		} finally {
			UserContext.remove(); // 清理线程变量的User对象
		}
	}

}
```

```java
@Configuration
public class UserContextCallbackConfiguration {

	@Bean
	public Collection<HystrixCallableWrapper> hystrixCallableWrappers() {
		Collection<HystrixCallableWrapper> wrappers = new ArrayList<>();
		wrappers.add(new HystrixCallableWrapper() {
			@Override
			public <V> Callable<V> wrap(Callable<V> callable) {
				return new UserContextCallable<V>(callable, UserContext.getUser());
			}
		});
		return wrappers;
	}

}
```

我参照网上的例子，已经实现了一个公共的HystrixConcurrencyStrategyCustom实现类，大家有兴趣可以查看sc.com.hystrix.concurrentstrategy包内的代码。

测试用例代码：

```java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HystrixTest3 {

	@Value("${local.server.port}")
	private int port;

	@Test
	public void test() {
		RestTemplate rest = new RestTemplate();
		User user = rest.getForObject("http://localhost:{port}/user3/1", User.class, this.getPort());
		System.out.println(user);
		Assert.assertNotNull(user);
	}

	public int getPort() {
		return port;
	}

}
```

### 测试hystrix+feign

如果feign要使用hystrix保护，则需要加入配置（默认feign不受hystrix保护）：

```yaml
feign: 
  httpclient: 
    # 使用apache httpclient
    enabled: true   
  hystrix:  
    # 开启hystrix+feign  
    enabled: true 
```

**读取超时时间(readTimeout)**

默认为1000ms，如果要定制feign+hystrix的超时时间，则由feign.client.config.default.readTimeout和hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds两者中最小值来决定。

```yaml
# hystrix 
hystrix: 
  command:
      default:
        execution:
          isolation:
            thread:
              timeoutInMilliseconds: 2000    
# feign              
feign: 
  httpclient: 
    # 使用apache httpclient
    enabled: true    
  client: 
    config: 
      # 定义全局的feign设置
      default: 
        connectTimeout: 1000
        readTimeout: 3000
  hystrix:  
    # 开启hystrix+feign  
    enabled: true   
```

如果上面的配置，则调用feign方法的超时时间为2000毫秒，如果你把hystrix的timeoutInMilliseconds修改为4000，则调用feign方法的超时时间为3000毫秒。

你可以通过发送请求：http://localhost:8300/user4/1?sleep=2800，调整sleep参数，来验证上面的配置。

**定制某个feign方法的超时时间**

例如某个方法处理时间比较长，需要大于默认值1000ms的超时处理时间，例如：文件上传处理。

因为默认情况下，ribbon、feign、hystrix的超时时间都是1000ms，因此我们只有调整配置，能让某个单独的feign方法调用(服务)，能稳定运行在xxxx ms就可以了，例如：uploadFile方法超时时间设置5000ms。

定制某个feign方法的hystrix参数比较费劲，需要先根据这个feign方法计算出对应的HystrixCommandKey，计算公式为：feign接口名#方法名(参数1类型,参数2类型,参数x类型)，只要一个字符不对也不行，因此最好的方法是调试调试feign.Feign类的configKey的方法，获取准确的HystrixCommandKey。

```java
  public static String configKey(Class targetType, Method method) {
    StringBuilder builder = new StringBuilder();
    builder.append(targetType.getSimpleName());
    builder.append('#').append(method.getName()).append('(');
    for (Type param : method.getGenericParameterTypes()) {
      param = Types.resolve(targetType, targetType, param);
      builder.append(Types.getRawType(param).getSimpleName()).append(',');
    }
    if (method.getParameterTypes().length > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.append(')').toString();
  }
```

例如：一个声明@FeignClient类的方法，如下，通过调试上面的configKey方法计算后的HystrixCommandKey为SampleServiceFeignClient#findByIdWithSleep(Long,Long)：

```java
@FeignClient(name = "sc-sampleservice")
public interface SampleServiceFeignClient {
	
	@RequestMapping(value = "/{id}", params = "sleep", method = RequestMethod.GET)
	User findByIdWithSleep(@PathVariable("id") Long id, @RequestParam(value = "sleep", required = false, defaultValue = "0") Long sleep);
	
}
```

获取到这个feign方法对应的HystrixCommandKey后，我们就可以为这个feign方法单独设置hystrix参数了，例如：读取超时时间。

```yaml
# hystrix 启动并发策略(自定义属性)
hystrix: 
  concurrent_strategy:  
    enabled: true
  command:
      SampleServiceFeignClient#findByIdWithSleep(Long,Long):
        execution:
          isolation:
            thread:
              timeoutInMilliseconds: 2000    
feign: 
  httpclient: 
    # 使用apache httpclient
    enabled: true    
  client: 
    config: 
      # 定义sc-sampleservice服务的feign设置
      sc-sampleservice: 
        connectTimeout: 1000
        readTimeout: 2000
  hystrix:  
    # 开启hystrix+feign  
    enabled: true 
```

因此遵照feign+hystrix组合使用最小配置值为超时时间的规则，因此我们要同时调整两个参数：hystrix的timeoutInMilliseconds和feign的readTimeout参数（参照上面的yml配置），否则无效。

参照上面yml，如果要调整feign方法SampleServiceFeignClient#findByIdWithSleep(Long,Long)的读取超时时间为2000ms，需要分别设置timeoutInMilliseconds: 2000 ，readTimeout: 2000(@FeignClient(name = "sc-sampleservice")声明服务;)，否则无效。

验证测试：

URL请求http://localhost:8300/user4/1?sleep=xxx，会触发HystrixTest4Controller调用hystrix保护的feign方法SampleServiceFeignClient#findByIdWithSleep(Long,Long)。

URL请求http://localhost:8300/user1/1?sleep=xxx，会触发HystrixTest1Controller调用hystrix保护的ribbon方法User findUser1ById(@PathVariable Long id, @RequestParam int sleep)。

发送请求，http://localhost:8300/user4/1?sleep=1800，返回正确的User信息。 证明单独的hystrix+feign某个方法调用超时配置有效。

发送请求，http://localhost:8300/user4/1?sleep=2100，报错或返回回退的User信息。证明单独的hystrix+feign某个方法调用超时配置有效。

发送请求，http://localhost:8300/user1/1?sleep=1800，报错或返回回退的User信息。证明单独的hystrix+feign某个方法调用超时配置有效，但系统的默认超时时间还是1000ms，也就是没有单独配置的方法受到默认值的控制。

### Hystrix监控

如果项目加入spring-boot-starter-actuator，就可以监控hystrix的运行情况，使用如下的URL持续监控(默认每隔500ms刷新一次)。

http://localhost:8300/hystrix.stream

关于Hystrix Dashboard可视化数据监控，可以查看sc-hystrix-turbine目录文档。

