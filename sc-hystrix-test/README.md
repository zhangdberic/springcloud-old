# 使用hystrix实现微服务的容错处理

当依赖的服务不可用时，服务自身会不会被拖垮？这是我们要考虑的问题。

## 理论

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

# hystrix配置

com.netflix.hystrix.HystrixCommandProperties

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

execution.isolation.thread.timeoutInMilliseconds=1000



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



### 测试Hystrix的线程上下文(ThreadLocal)传递

如果hystrix基于的THREAD模式，则ThreadLocal中的值使用无法传递到@HystrixCommand声明的方法，因为隶属两个不同的线程。







