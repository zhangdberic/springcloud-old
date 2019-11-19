package com.sc.sampleservice.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.ContentCachingResponseWrapper;
/**
 * 如需要测试响应压缩则可以开启本类
 * @author zhangdb
 *
 */
@Configuration
public class UserControllerJavaConfig {

	@Bean
	@ConditionalOnProperty(value="server.response.content-length",matchIfMissing=false)
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new Filter() {
			@Override
			public void init(FilterConfig filterConfig) throws ServletException {
			}

			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
				ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
		        chain.doFilter(request, responseWrapper);
		        responseWrapper.copyBodyToResponse();
			}

			@Override
			public void destroy() {
			}
		});
		List<String> urls = new ArrayList<String>();
		urls.add("/*");
		filterRegistrationBean.setUrlPatterns(urls);
		filterRegistrationBean.setName("response-content-length");
		filterRegistrationBean.setOrder(1);
		return filterRegistrationBean;
	}

}
