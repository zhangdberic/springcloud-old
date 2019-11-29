package com.sc.seata.bztest1.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sc.seata.bztest1.domain.User;
import com.sc.seata.bztest1.service.BusinessService;

@RestController
public class SeataBzTest1Controller {

    @Autowired
    private BusinessService businessService;

	@RequestMapping(name = "/adduser", produces = "application/json;charset=UTF-8")
	public Collection<User> addUsers(@RequestParam("userId") Long userId,
            @RequestParam("name") String name) {
		return this.businessService.addUser(userId, name);
	}

}
