package com.sc.zipkin.test1.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.sc.zipkin.test1.domain.User;

@RestController
public class TestZipkin1Controller {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(TestZipkin1Controller.class);

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id) {
		User user = this.restTemplate.getForObject("http://sc-zipkin-test2/user/{id}", User.class, id);
		logger.info("find user[{}] by id[{}].", user, id);
		return user;
	}

}
