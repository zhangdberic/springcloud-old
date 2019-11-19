package com.sc.feign.serviceclient;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sc.feign.domain.User;

@FeignClient(name = "sc-sampleservice")
public interface SampleServiceFeignClient {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	User findById(@PathVariable("id") Long id);

	@RequestMapping(value = "/{id}", params = "sleep", method = RequestMethod.GET)
	User findByIdWithSleep(@PathVariable("id") Long id, @RequestParam(value = "sleep", required = false, defaultValue = "0") Long sleep);
	
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	List<User> findUsers(@RequestParam(value="num",required=false,defaultValue="10") int num);
	
	@RequestMapping(value = "/add_users", method = RequestMethod.POST)
	List<User> addUsers(List<User> users);
	

}
