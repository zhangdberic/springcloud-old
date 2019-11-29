package com.sc.seata.mstest2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sc.seata.mstest2.domain.User;
import com.sc.seata.mstest2.service.UserService;



@RestController
public class SeataMicroserviceTest2Controller {
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/addUser")
	public User addUser(@RequestParam("userId") Long userId,
            @RequestParam("name") String name) {
		User user = new User(userId,name);
		user = this.userService.addUser(user);
		return user;
	}

}
