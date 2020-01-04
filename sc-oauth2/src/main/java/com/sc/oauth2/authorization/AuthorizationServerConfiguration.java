package com.sc.oauth2.authorization;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * 认证服务器配置
 * @author Administrator
 *
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {
	/** 属性配置 */
	@Autowired
	private PropertiesConfiguration propertiesConfiguration;

	/** 认证管理器 */
	@Autowired
	private AuthenticationManager authenticationManager;

	/** 用户信息服务 */
	@Autowired
	private UserServiceDetail userServiceDetail;

	/** redis连接池工厂  */
	@Autowired
	private RedisConnectionFactory redisConnectionFactory;
	
	/** 数据库连接池 */
	@Autowired
	private DataSource dataSource;

	/**
	 * token存储
	 * @return
	 */
	@Bean
	public TokenStore tokenStore() {
		return new RedisTokenStore(this.redisConnectionFactory);
	}

	/**
	 * 客户端信息服务
	 * @return
	 */
	@Bean
	public ClientDetailsService clientDetailsService() {
		JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(this.dataSource);
		return jdbcClientDetailsService;
	}

	/**
	 * token服务(操作)
	 * @return
	 */
	@Bean
	@Primary
	public AuthorizationServerTokenServices tokenService() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setTokenStore(tokenStore());
		tokenServices.setSupportRefreshToken(this.propertiesConfiguration.isSupportRefreshToken());
		tokenServices.setReuseRefreshToken(this.propertiesConfiguration.isReuseRefreshToken());
		tokenServices.setAccessTokenValiditySeconds(this.propertiesConfiguration.getAccessTokenValiditySeconds());
		tokenServices.setRefreshTokenValiditySeconds(this.propertiesConfiguration.getRefreshTokenValiditySeconds());
		tokenServices.setClientDetailsService(clientDetailsService());
		return tokenServices;
	}

	/**
	 * 密码编码器
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(this.clientDetailsService());
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(this.tokenStore());
		endpoints.authenticationManager(this.authenticationManager);
		endpoints.userDetailsService(this.userServiceDetail);
		endpoints.tokenServices(this.tokenService());
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		// 允许表单认证
		security.allowFormAuthenticationForClients();
		// 设置请求URL：/oauth/tokey_key和/oauth/check_token的安全规则
		security.tokenKeyAccess("denyAll()").checkTokenAccess("isAuthenticated()");
		// 设置client_secret的密码编码器，如果你的数据库中对client_secret加密了则要设置相同的加密器
		security.passwordEncoder(this.passwordEncoder());
	}
}
