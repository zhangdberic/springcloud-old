package com.sc.oauth2.password_authorization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {
	
	@Autowired
	private RestTemplate oauth2RestTemplate;
	
	@GetMapping("/pw/test/1")
	public String test1() {
		OAuth2Authentication oauth2Authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		System.out.println(oauth2Authentication);
		String pw1TestResult = this.oauth2RestTemplate.getForObject("http://SC-OAUTH2-PW-SERVICECLIENT1//pw1/test/1", String.class);
		return "success, "+pw1TestResult;
	}

}
