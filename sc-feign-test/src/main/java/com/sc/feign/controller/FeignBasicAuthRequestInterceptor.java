package com.sc.feign.controller;

import java.nio.charset.Charset;

import feign.auth.BasicAuthRequestInterceptor;

public class FeignBasicAuthRequestInterceptor extends BasicAuthRequestInterceptor {

	public FeignBasicAuthRequestInterceptor() {
		super("黑哥", "密码", Charset.forName("UTF-8"));
	}

}
