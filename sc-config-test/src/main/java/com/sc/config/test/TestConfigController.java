package com.sc.config.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestConfigController {

	@Autowired
	private ConfigProperties configProperties;

	@GetMapping("/getConfigProperties")
	public ConfigProperties getConfigProperties() {
		ConfigProperties configProperties = new ConfigProperties();
		configProperties.setRefreshProperty1(this.configProperties.getRefreshProperty1());
		configProperties.setEncryptedProperty2(this.configProperties.getEncryptedProperty2());
		return configProperties;
	}

}
