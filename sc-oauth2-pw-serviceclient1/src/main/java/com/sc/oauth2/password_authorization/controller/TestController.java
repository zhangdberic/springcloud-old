package com.sc.oauth2.password_authorization.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@GetMapping("/pw1/test/1")
	public String test1() {
		OAuth2Authentication oauth2Authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		System.out.println(oauth2Authentication);		
		return "success_pw1_test1";
	}

}
