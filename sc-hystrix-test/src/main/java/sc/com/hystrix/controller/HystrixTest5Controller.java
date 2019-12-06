package sc.com.hystrix.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import sc.com.hystrix.domain.User;

@RestController
public class HystrixTest5Controller {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(HystrixTest5Controller.class);

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * 测试hystrix线程池配置
	 * @param id
	 * @param sleep 休眠时间
	 * @return
	 */
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

	/**
	 * 回退方法(降级方法)
	 * @param id
	 * @param sleep
	 * @return
	 */
	public User findUser5ByIdFallback(Long id, int sleep) {
		logger.info("into fallback[{}].");
		User user = new User();
		user.setId(-1l);
		user.setName("默认用户");
		return user;
	}

}
