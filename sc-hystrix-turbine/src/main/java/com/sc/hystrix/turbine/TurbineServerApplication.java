package com.sc.hystrix.turbine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.turbine.stream.EnableTurbineStream;
/**
 * Hystrix Dashboard监控的turbine地址：http://192.168.5.78:28031/turbine.stream
 * @author zhangdb
 *
 */
@SpringBootApplication
@EnableTurbineStream
public class TurbineServerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TurbineServerApplication.class, args);
	}

}
