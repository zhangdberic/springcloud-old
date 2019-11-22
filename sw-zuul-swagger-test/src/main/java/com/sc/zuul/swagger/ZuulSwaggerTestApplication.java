package com.sc.zuul.swagger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class ZuulSwaggerTestApplication {
	
	
	public static void main(String[] args) {
		SpringApplication.run(ZuulSwaggerTestApplication.class, args);
	}


}
