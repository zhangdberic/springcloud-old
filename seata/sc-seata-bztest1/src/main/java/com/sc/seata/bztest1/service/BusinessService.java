package com.sc.seata.bztest1.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sc.seata.bztest1.domain.User;
import com.sc.seata.bztest1.feign.Mstest1FeignClient;
import com.sc.seata.bztest1.feign.Mstest2FeignClient;

import io.seata.spring.annotation.GlobalTransactional;

@Service
public class BusinessService {

	@Autowired
	private Mstest1FeignClient mstest1FeignClient;

	@Autowired
	private Mstest2FeignClient mstest2FeignClient;

	@GlobalTransactional(name="addUser")
	public Collection<User> addUser(Long userId, String name) {
		User user1 = mstest1FeignClient.addUser(userId, name);
		User user2 = mstest2FeignClient.addUser(userId, name);
		Collection<User> users = new ArrayList<User>();
		users.add(user1);
		users.add(user2);
		return users;
	}

}
