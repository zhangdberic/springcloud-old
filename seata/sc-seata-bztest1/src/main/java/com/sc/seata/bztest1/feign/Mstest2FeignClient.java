package com.sc.seata.bztest1.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sc.seata.bztest1.domain.User;

@FeignClient(name = "mstest2", url = "127.0.0.1:8102")
public interface  Mstest2FeignClient {
	
	@GetMapping("/addUser")
	User addUser(@RequestParam("userId") Long userId,
            @RequestParam("name") String name);
}

