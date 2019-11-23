package sc.com.hystrix.controller;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import sc.com.hystrix.domain.User;

@RestController
public class HystrixTest1Controller {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(HystrixTest1Controller.class);

	private final AtomicInteger failureCount = new AtomicInteger(0);

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * 测试如下情况，需要编写测试用例，{@link}
	 * 5000毫秒内超出20次请求失败则激活短路器（用于测试如果激活短路器），默认值(5000毫秒20次)
	 * 再再发送10个正确的请求全部回退（用于测试短路器打开的情况下正确的请求也会被拒绝）
	 * 休眠10000毫秒发送一个请求通过短路器（测试短路器打开的状态下经过一段时间，允许1个请求通过），默认值(10000毫秒1次)
	 * 默认值，可以看{@link HystrixCommandProperties}
	 * @param id
	 * @return
	 */
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

}
