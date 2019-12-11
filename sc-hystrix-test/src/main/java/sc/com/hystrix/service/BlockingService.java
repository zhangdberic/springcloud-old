package sc.com.hystrix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import sc.com.hystrix.domain.User;

@Service
public class BlockingService {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(BlockingService.class);	
	@Autowired
	private RestTemplate restTemplate;
	
	@HystrixCommand(fallbackMethod = "findUser1ByIdFallback")
	public User findUser1ById(@PathVariable Long id, @RequestParam int sleep) {
		logger.info("request param sleep[{}].", sleep);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
	}
	
	public User findUser1ByIdFallback(Long id, int sleep) {
		logger.info("into fallback[{}].");
		User user = new User();
		user.setId(-1l);
		user.setName("默认用户");
		return user;
	}	

}
