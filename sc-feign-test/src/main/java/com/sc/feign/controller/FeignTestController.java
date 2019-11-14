package com.sc.feign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sc.feign.domain.User;
import com.sc.feign.serviceclient.SampleServiceFeignClient;

@RestController
public class FeignTestController {
	
	@Autowired
	private SampleServiceFeignClient sampleServiceFeignClient;
	
	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id) {
		return this.sampleServiceFeignClient.findById(id);
	}

}
