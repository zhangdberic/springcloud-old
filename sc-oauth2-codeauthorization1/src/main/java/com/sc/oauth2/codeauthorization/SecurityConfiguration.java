package com.sc.oauth2.codeauthorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * web相关安全配置类
 * 用于服务(web)的安全配置，区别于ResourceServerConfiguration用于服务(service)的安全配置。
 * 主要用于授权码模式(authorization_code)的安全规则配置。
 * @author Administrator
 *
 */
@Configuration
@EnableWebSecurity
@EnableOAuth2Sso
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Value("${security.oauth2.client.client-id}")
	private String clientId;

	@Override
	public void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.csrf().disable().authorizeRequests().antMatchers("/login").permitAll().
		and().logout().logoutSuccessUrl("http://localhost:6001/auth/exit?clientId="+this.clientId).
		and().authorizeRequests().antMatchers("/**").authenticated();
		// @formatter:on
	}

	@Override
	public void configure(WebSecurity web) throws Exception {

	}

}
