package sc.com.hystrix.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import sc.com.hystrix.domain.User;

@RestController
public class HystrixTest3Controller {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private HystrixConcurrentStrategyTestBean hystrixConcurrentStrategyTestBean;
	

	/**
	 * 测试hystrix的ThreadLocal内容传递性(hystrix并发策略)
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/user3/{id}")
	public User findUser3ById(@PathVariable Long id) {
		User user =  this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
		UserContext.setUser(user);
		user = this.hystrixConcurrentStrategyTestBean.findUser3Hystrix();
		UserContext.remove();
		return user;
	}
	

}
