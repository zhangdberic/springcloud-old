package com.sc.sampleservice.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sc.sampleservice.dao.UserRepository;
import com.sc.sampleservice.domain.User;

@RestController
public class UserController {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;

	@GetMapping(value = "/{id}", produces = "application/json;charset=UTF-8")
	public User findById(@PathVariable Long id) throws IOException {
		User user = this.userRepository.findOne(id);
		logger.info("finded User:" + user);
		return user;
	}

}
