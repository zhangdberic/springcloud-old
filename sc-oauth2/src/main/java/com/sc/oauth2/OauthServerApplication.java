package com.sc.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * oauth认证服务器
 * @author Administrator
 *
 */
@SpringBootApplication
public class OauthServerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(OauthServerApplication.class, args);
	}

}
