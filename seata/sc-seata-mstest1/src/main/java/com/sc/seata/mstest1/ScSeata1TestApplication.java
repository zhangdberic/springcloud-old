package com.sc.seata.mstest1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories
public class ScSeata1TestApplication {

	public static void main(String[] args) {
		 SpringApplication.run(ScSeata1TestApplication.class, args);

	}

}
