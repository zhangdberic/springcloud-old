package com.sc.config.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class ConfigProperties {
	
	@Value("${config.property.refreshProperty1}")
	private String refreshProperty1;
	
	@Value("${config.property.encryptedProperty2}")
	private String encryptedProperty2;

	public String getRefreshProperty1() {
		return refreshProperty1;
	}

	public void setRefreshProperty1(String refreshProperty1) {
		this.refreshProperty1 = refreshProperty1;
	}

	public String getEncryptedProperty2() {
		return encryptedProperty2;
	}

	public void setEncryptedProperty2(String encryptedProperty2) {
		this.encryptedProperty2 = encryptedProperty2;
	}
	
	
}
