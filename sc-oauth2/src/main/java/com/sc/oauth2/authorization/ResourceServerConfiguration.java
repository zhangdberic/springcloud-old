package com.sc.oauth2.authorization;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * 资源服务安全配置类
 * 用于服务(service)的安全规则配置，区别于SecurityConfiguration用于web的安全规则配置。
 * 主要用于密码模式(password)的安全规则配置。
 * @author Administrator
 *
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
	
	/**
	 * 自定义安全配置
	 * 注意：一定要以http.antMatcher(...)方法开头匹配，否则会覆盖SecurityConfiguration类的相关配置.
	 * 这里定义的配置有两个作用：
	 * 1.安全限制，定义外界请求访问系统的安全策略。
	 * 2.根据规则生成过滤链(FilterChainProxy,过滤器的排列组合)，不同的规则生成的过滤链不同的。
	 * 系统默认的/oauth/xxx相关请求也是基于ResourceServerConfiguration实现的,只不过系统默认已经配置完了。
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {
		// 提供给client端用于认证
		http.antMatcher("/auth/user").authorizeRequests().anyRequest().authenticated();
//		// 管理器-管理ClientDetail
//		http.antMatcher("/clients").authorizeRequests().antMatchers(HttpMethod.POST).hasAnyAuthority("oauthmanager-client-add","role_oauthmanager_client","role_oauthmanager_admin");
//		http.antMatcher("/clients/*").authorizeRequests().antMatchers(HttpMethod.GET).hasAnyAuthority("oauthmanager-client-get","role_oauthmanager_client","role_oauthmanager_admin");
//		http.antMatcher("/clients/*").authorizeRequests().antMatchers(HttpMethod.PUT).hasAnyAuthority("oauthmanager-client-update","role_oauthmanager_client","role_oauthmanager_admin");
//		http.antMatcher("/clients/*").authorizeRequests().antMatchers(HttpMethod.DELETE).hasAnyAuthority("oauthmanager-client-delete","role_oauthmanager_client","role_oauthmanager_admin");
//		// 管理器-管理User
//		http.antMatcher("/users").authorizeRequests().antMatchers(HttpMethod.POST).hasAnyAuthority("oauthmanager-user-add","role_oauthmanager_user","role_oauthmanager_admin");
//		http.antMatcher("/users/*").authorizeRequests().antMatchers(HttpMethod.GET).hasAnyAuthority("oauthmanager-user-get","role_oauthmanager_user","role_oauthmanager_admin");
//		http.antMatcher("/users/*").authorizeRequests().antMatchers(HttpMethod.PUT).hasAnyAuthority("oauthmanager-user-update","role_oauthmanager_user","role_oauthmanager_admin");
//		http.antMatcher("/users/*").authorizeRequests().antMatchers(HttpMethod.DELETE).hasAnyAuthority("oauthmanager-user-delete","role_oauthmanager_user","role_oauthmanager_admin");
//		http.antMatcher("/users/*/authorites").authorizeRequests().antMatchers(HttpMethod.POST).hasAnyAuthority("oauthmanager-user_authority-add","role_oauthmanager_user","role_oauthmanager_admin");
//		http.antMatcher("/users/*/authorites").authorizeRequests().antMatchers(HttpMethod.GET).hasAnyAuthority("oauthmanager-user_authority-get","role_oauthmanager_user","role_oauthmanager_admin");
//		http.antMatcher("/users/*/authorites").authorizeRequests().antMatchers(HttpMethod.DELETE).hasAnyAuthority("oauthmanager-user_authority-delete","role_oauthmanager_user","role_oauthmanager_admin");
//		// 管理器-管理Authority
//		http.antMatcher("/authorites").authorizeRequests().antMatchers(HttpMethod.POST).hasAnyAuthority("oauthmanager-authority-add","role_oauthmanager_authority","role_oauthmanager_admin"); 
//		http.antMatcher("/authorites/*").authorizeRequests().antMatchers(HttpMethod.GET).hasAnyAuthority("oauthmanager-authority-get","role_oauthmanager_authority","role_oauthmanager_admin");
//		http.antMatcher("/authorites/*").authorizeRequests().antMatchers(HttpMethod.PUT).hasAnyAuthority("oauthmanager-authority-update","role_oauthmanager_authority","role_oauthmanager_admin");
//		http.antMatcher("/authorites/*").authorizeRequests().antMatchers(HttpMethod.DELETE).hasAnyAuthority("oauthmanager-authority-delete","role_oauthmanager_authority","role_oauthmanager_admin");
		
	}
}
