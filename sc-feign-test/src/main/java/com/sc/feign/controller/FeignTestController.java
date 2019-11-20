package com.sc.feign.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sc.feign.domain.UploadInfo;
import com.sc.feign.domain.User;
import com.sc.feign.serviceclient.SampleServiceFeignClient;

@RestController
public class FeignTestController {
	private static final Logger logger = LoggerFactory.getLogger(FeignTestController.class);
	
	@Autowired
	private SampleServiceFeignClient sampleServiceFeignClient;
	
	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id) {
		return this.sampleServiceFeignClient.findById(id);
	}
	
	@GetMapping(value="/user/{id}",params = "sleep")
	public User findByIdWithSleep(@PathVariable("id") Long id, @RequestParam(value = "sleep", required = false, defaultValue = "0") Long sleep) {
		return this.sampleServiceFeignClient.findByIdWithSleep(id, sleep);
	}
	
	@GetMapping(value = "/users")
	public List<User> findUsers(@RequestParam(value="num",required=false,defaultValue="10") int num) {
		return this.sampleServiceFeignClient.findUsers(num);
	}
	
	@RequestMapping(value = "/add_users", method = RequestMethod.POST, consumes="application/json;charset=UTF-8",produces = "application/json;charset=UTF-8")
	public List<User> addUsers(@RequestBody List<User> users){
		logger.info("param users row num:"+(users==null?0:users.size()));
		return this.sampleServiceFeignClient.addUsers(users);
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST, consumes="application/json;charset=UTF-8",produces = "application/json;charset=UTF-8")
	public User addUser(@RequestBody User user) {
		return this.sampleServiceFeignClient.addUser(user);
	}
	
	@PostMapping(value="/uploadFile", produces = "application/json;charset=UTF-8")
	public UploadInfo uploadLargeFile(@RequestPart(value="file") MultipartFile file) {
		return this.sampleServiceFeignClient.handleFileUpload(file);
	}
	

}
