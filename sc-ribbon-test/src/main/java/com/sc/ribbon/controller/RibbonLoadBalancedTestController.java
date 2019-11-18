package com.sc.ribbon.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.sc.ribbon.domain.User;

/**
 * 测试ribbon的loadbalancer
 * 其会对sc-sampleservice发起服务请求,如果sc-sampleservice部署了两个(集群),则会测试负载均衡请求分发情况.
 * 
 * @author zhangdb
 *
 */
@RestController
public class RibbonLoadBalancedTestController {

	private static final Logger logger = LoggerFactory.getLogger(RibbonLoadBalancedTestController.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LoadBalancerClient loadBalancerClient;

	@GetMapping(value = "/user/{id}", params = "sleep")
	public User findByIdWithSleep(@PathVariable Long id, @RequestParam(value = "sleep", required = false, defaultValue = "0") Long sleep) throws IOException {
		long beginTime = System.currentTimeMillis();
		try {
			User user = this.restTemplate.getForObject("http://sc-sampleservice/{id}?sleep={sleep}", User.class, id, sleep);
			return user;
		} finally {
			logger.info("findByIdWithSleep spend[{}]mills.", System.currentTimeMillis() - beginTime);
		}
	}

	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id) {
		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
	}

	@PostMapping(value = "/", produces = "application/json;charset=UTF-8")
	public User addUser(@RequestBody User user) {
		User savedUser = this.restTemplate.postForObject("http://sc-sampleservice/", user, User.class);
		return savedUser;
	}

	@GetMapping(value = "/log-user-instance", produces = "text/plant;charset=utf-8")
	public String logUserInstance() {
		ServiceInstance serviceInstance = this.loadBalancerClient.choose("sc-sampleservice");
		String balancerInfo = "serviceId:" + serviceInstance.getServiceId() + ", target " + serviceInstance.getHost() + ":" + serviceInstance.getPort();
		logger.info("{}:{}:{}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort());
		return balancerInfo;
	}

}
