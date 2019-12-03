package sc.com.hystrix.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import sc.com.hystrix.domain.User;

@Component
public class HystrixConcurrentStrategyTestBean {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(HystrixConcurrentStrategyTestBean.class);
	
	
	@HystrixCommand(fallbackMethod = "findUser3HystrixFallback")
	public User findUser3Hystrix() {
		User user = UserContext.getUser();
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
