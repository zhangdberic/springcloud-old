package com.sc.seata.mstest2.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sc.seata.mstest2.domain.User;
import com.sc.seata.mstest2.feign.Mstest3FeignClient;
import com.sc.seata.mstest2.respository.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private Mstest3FeignClient mstest3FeignClient;
	
	@Transactional
    public User addUser(User user) {
		User user2 =  this.userRepository.saveAndFlush(user);
		User user3 = this.mstest3FeignClient.addUser(user.getId(),user.getName());
		user3.setName(user2.getName()+"_"+user3.getName());
		return user3;
	}

}
