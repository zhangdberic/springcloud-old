package com.sc.zipkin.test2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
/**
 * zipkin服务器端
 * @author zhangdb
 *
 */
@SpringBootApplication
public class ZipkinService2Application {
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}	
		
	
	public static void main(String[] args) {
		SpringApplication.run(ZipkinService2Application.class, args);
	}

}
