package com.sc.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * spring cloud config 启动器
 * # 启动时指定环境 --spring.profiles.active=test 
 * # 测试URL：http://{ip}:{port}/{application}/{default,dev,test,prod}，
 * # 例如：http://localhost:8080/microservice-foo/dev
 * @author zhangdb
 *
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
