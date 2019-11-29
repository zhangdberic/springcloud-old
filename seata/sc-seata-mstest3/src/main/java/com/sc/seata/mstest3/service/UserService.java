package com.sc.seata.mstest3.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sc.seata.mstest3.domain.User;
import com.sc.seata.mstest3.respository.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	
	@Transactional
    public User addUser(User user) {
		return this.userRepository.saveAndFlush(user);
	}

}
