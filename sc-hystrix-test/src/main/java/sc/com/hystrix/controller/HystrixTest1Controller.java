package sc.com.hystrix.controller;

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

	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * 测试超时
	 * @param id
	 * @param sleep 休眠时间
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "findUser1ByIdFallback")
	//@HystrixCommand(fallbackMethod = "findUser1ByIdFallback",commandProperties= {@HystrixProperty(name="execution.isolation.strategy",value="SEMAPHORE")})
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

}
