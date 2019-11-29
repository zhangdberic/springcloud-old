package com.sc.seata.mstest2.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sc.seata.mstest2.domain.User;

@FeignClient(name = "mstest1", url = "127.0.0.1:8103")
public interface  Mstest3FeignClient {
	
	@GetMapping("/addUser")
	User addUser(@RequestParam("userId") Long userId,
            @RequestParam("name") String name);
}

