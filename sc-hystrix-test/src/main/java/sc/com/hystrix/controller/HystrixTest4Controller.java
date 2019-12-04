package sc.com.hystrix.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sc.com.hystrix.domain.User;

@RestController
public class HystrixTest4Controller {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(HystrixTest1Controller.class);

	
	@Autowired
	private SampleServiceFeignClient sampleServiceFeignClient;
	
	/**
	 * 测试feign和hystrix组合
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/user4/{id}")
	public User findUser1ById(@PathVariable Long id, @RequestParam long sleep) {
		logger.info("request param sleep[{}].", sleep);
		return this.sampleServiceFeignClient.findByIdWithSleep(id, sleep);
	}

	



}
