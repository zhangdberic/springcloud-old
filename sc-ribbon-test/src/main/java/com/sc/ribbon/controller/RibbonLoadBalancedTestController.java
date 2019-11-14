package com.sc.ribbon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id) {
		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
	}

	@GetMapping(value="/log-user-instance",produces="text/plant;charset=utf-8")
	public String logUserInstance() {
		ServiceInstance serviceInstance = this.loadBalancerClient.choose("sc-sampleservice");
		String balancerInfo = "serviceId:"+serviceInstance.getServiceId()+", target "+serviceInstance.getHost()+":"+serviceInstance.getPort();
		logger.info("{}:{}:{}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort());
		return balancerInfo;
	}

}
