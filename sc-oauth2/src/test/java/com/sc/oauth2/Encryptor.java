package com.sc.oauth2;

import org.junit.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public class Encryptor {
	
	@Test
	public void encrypt() {
		System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("123456"));
	}

}
