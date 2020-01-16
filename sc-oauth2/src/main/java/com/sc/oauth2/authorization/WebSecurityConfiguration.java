package com.sc.oauth2.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		AuthenticationManager manager = super.authenticationManagerBean();
		return manager;
	}

	/**
	 * 自定义安全配置
	 * 注意：不应以http.antMatcher(...)方法开头匹配，否则会和ResourceServerConfiguration安全规则配置冲突，应以authorizeRequests()开头.
	 * 这里定义的配置有两个作用：
	 * 1.安全限制，定义外界请求访问系统的安全策略。
	 * 2.根据规则生成过滤链(FilterChainProxy,过滤器的排列组合)，不同的规则生成的过滤链不同的。
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {
		// 授权码模式(authorization_code)配置
		// 授权码模式下,会用到/oauth/authorize(授权URL)、/login(登录页)、/oauth/confirm_access(用户授权确认),
		// 但由于/oauth/**相关的请求安全规则配置由系统默认生成,则无需再配置。
		//
		// @formatter:off
		http.authorizeRequests().antMatchers("/login","/logout").permitAll().and().formLogin().permitAll();
		// @formatter:on

	}
	
	/**
	 * 除了http,其它css、js和图片等静态文件访问控制
	 */
    @Override
    public void configure(WebSecurity web) throws Exception {
    }

}
