package com.sc.seata.bztest1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@SpringBootApplication
@org.springframework.cloud.openfeign.EnableFeignClients
public class SeataBzTestApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(SeataBzTestApplication.class, args);
	}

}
