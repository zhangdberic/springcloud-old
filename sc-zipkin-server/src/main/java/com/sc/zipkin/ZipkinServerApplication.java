package com.sc.zipkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import zipkin.server.internal.EnableZipkinServer;
/**
 * zipkin服务器端
 * @author zhangdb
 *
 */
@SpringBootApplication
@EnableZipkinServer
public class ZipkinServerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ZipkinServerApplication.class, args);
	}

}
