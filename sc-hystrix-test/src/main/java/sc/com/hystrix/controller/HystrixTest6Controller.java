package sc.com.hystrix.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sc.com.hystrix.domain.User;
import sc.com.hystrix.service.BlockingService;

@RestController
public class HystrixTest6Controller {
	
	@Autowired
	private BlockingService blockingService;

	/**
	 * 测试调用hystrix保护的方法(线程池策略),是否阻塞原始的调用线程,测试结果阻塞
	 * @param id
	 * @param sleep 休眠时间
	 * @return
	 */
	@GetMapping(value = "/user6/{id}")
	public User findUser1ById(@PathVariable Long id, @RequestParam int sleep) {
		System.out.println("=============================");
		System.out.println(Thread.currentThread());
		User user =  this.blockingService.findUser1ById(id, sleep);
		System.out.println(Thread.currentThread());
		return user;

	}

}
