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
public class HystrixTest2Controller {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(HystrixTest2Controller.class);
	/** 失败计数器 */
	private final AtomicInteger failureCount = new AtomicInteger(0);

	@Autowired
	private RestTemplate restTemplate;
	

	/**
	 * 配合测试用例@see sc.com.hystrix.HystrixTest2，来测试，
	 * 断路器打开（跳闸）、保护机制、回退机制、自我修复。
	 * 
	 * @param id
	 * @param status status=true标识为成功请求，status=failure标识为失败请求。
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
	 * @param status
	 * @return
	 */
	public User findUser2ByIdFallback(Long id, Boolean status) {
		logger.info("into fallback[{}].", this.failureCount.get());
		User user = new User();
		user.setId(-1l);
		user.setName("默认用户");
		return user;
	}
	

}
