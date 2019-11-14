package com.sc.sampleservice.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sc.sampleservice.dao.UserRepository;
import com.sc.sampleservice.domain.UploadInfo;
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

	@GetMapping(value = "/{id}", params = "sleep", produces = "application/json;charset=UTF-8")
	public User findByIdWithSleep(@PathVariable Long id, @RequestParam(value = "sleep", required = false, defaultValue = "0") Long sleep) throws IOException {
		try {
			logger.info("sleep:{}",sleep);
			Thread.sleep(sleep);
		} catch (InterruptedException ex) {
			throw new IOException(ex);
		}
		User user = this.userRepository.findOne(id);
		logger.info("finded User:" + user);
		return user;
	}
	
	@GetMapping(value = "/users", produces = "application/json;charset=UTF-8")
	public List<User> findUsers(@RequestParam(value="num",required=false,defaultValue="10") int num){
		List<User> users = new ArrayList<User>();
		for(int i=0;i<num;i++) {
			User user = this.userRepository.findOne(1l);
			user.setId(new Long(i));
			users.add(user);
		}
		return users;
	}
	
	@PostMapping(value="/", produces = "application/json;charset=UTF-8")
	public User addUser(@RequestBody User user) {
		User savedUser = this.userRepository.saveAndFlush(user);
		return savedUser;
	}
	
	@PostMapping(value="/uploadFile", produces = "application/json;charset=UTF-8")
	public UploadInfo uploadLargeFile(@RequestPart(value="file") MultipartFile file) {
		UploadInfo uploadInfo = new UploadInfo();
		uploadInfo.setName(file.getName());
		uploadInfo.setOriginalFilename(file.getOriginalFilename());
		uploadInfo.setSize(file.getSize());
		return uploadInfo;
	}

}
