# oauth2 代码认证模式(code authorization)

不需要用户手工认证页面，则设置数据库：

OAUTH_CLIENT_DETAILS表的AUTOAPPROVE的字段为true。



```yaml
server:
  session:
    cookie:
      name: testclient
```

oauth2的客户端，必须重新设置session的cookie名称，否则在同一域名内会相互覆盖。因为默认spring boot的默认session cookie名称为JSESSIONID，而你为了完成单点登录需要三个项目，oauth2服务器，oauth2客户端1，oauth2客户端2，如果三个项目都使用JSESSION的cookie名称，并且在同一关域内（例如：测试为location），那么三个项目的session cookie会相互覆盖，因此必须为两个测试oauth2客户端重命名session的cookie名称。



最关键的SecurityConfiguration类

```java
@Configuration
@EnableWebSecurity
@EnableOAuth2Sso
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/login").permitAll().
		and().authorizeRequests().antMatchers("/**").authenticated();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {

	}

}
```



application.yml

```yaml
server:
  session:
    cookie:
      name: testclient
security:
  oauth2:
    client:
     # grant-type: client_credentials    # 授权模式
      client-id: test_client        # 在oauth 服务端注册的client-id
      client-secret: 123456     # 在oauth 服务端注册的secret
      access-token-uri: http://localhost:6001/oauth/token    #获取token 地址
      user-authorization-uri: http://localhost:6001/oauth/authorize  # 认证地址
      scope: web
    resource:
      loadBalanced: true
      token-info-uri: http://localhost:6001/oauth/check_token  # 检查token
      user-info-uri: http://localhost:6001/auth/user   # 用户信息
    sso:
      login-path: /login      
```

