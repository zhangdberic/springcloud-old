# oauth2服务器端

## 1.oauth2的基本理论

oauth2的理论网上很多，你可以baidu一下。这里不多说了。

## 2.oauth2配置

当前的代码例子，实现了基于jdbc存储client_details、user、authority等静态数据，基于redis存放token。

一般情况下，当前的示例代码基本就够用了。

### 2.1 pom.xml

```xml
		<!-- spring cloud security oauth2 -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.security.oauth</groupId>
			<artifactId>spring-security-oauth2</artifactId>
		</dependency>

		<!-- oauth2 jdbc redis -->	
		<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>        
		<dependency>
			<groupId>com.oracle</groupId>
			<artifactId>ojdbc6</artifactId>
			<version>11.2.0.4.181016</version>
		</dependency>  
```

<!-- spring cloud security oauth2 -->注释的依赖包是spring cloud oauth2必须的依赖包，而下面的<!-- oauth2 jdbc redis -->注释的依赖包是基于jdbc存储client_details、user、authority静态数据，基于redis存放token的依赖包。

### 2.2 OauthServerApplication

```java
@SpringBootApplication
public class OauthServerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(OauthServerApplication.class, args);
	}

}
```

OauthServerApplication没有什么特殊的源注释(配置)，特殊的源注释在其它security相关类上声明。

### 2.3 AuthorizationServerConfiguration

spring cloud oauth2的服务器端bean配置，注意：类上的@EnableAuthorizationServer源注释，开启oauth2 server端的bean自动配置。

```java
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
```



### 2.4 ResourceServerConfiguration

服务资源授权配置，理解为：对外提供服务的URI授权，例如：oauth2服务器对外提供的/auth/user服务
(用于获取已登录用户的基本信息和权限)，这个URI需要授权才可以访问到。

注意：在ResourceServerConfiguration和WebSecurityConfiguration两个类同时存在的时候，ResourceServerConfiguration的HttpSecurity配置一定要以http.antMatcher("xxx")开头来定义资源授权，否则配置无效。

oauth2 server原生提供的/oauth2/* 相关URI(服务)不需要在这里配置授权，oauth2 server默认已经为/oauth2/*相关的URL定义了授权规则，一般情况下无须修改，例如：/oauth2/token授权为permitAll()、/oauth/tokey_key授权为denyAll()，但可以通过security.tokenKeyAccess("denyAll()")来重新设置、/oauth/check_token为denyAll()，但可以通过security.checkTokenAccess("isAuthenticated()")来重新设置。

```java
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
	}
}

```

### 2.5 WebSecurityConfiguration

web资源授权配置，用于配置oauth2 server的web授权，这里配置的授权的都是oauth2 server对外提供的web请求，例如：登录(/login)，登录(/logout)，js，css等。并且，这个配置器还可以配置安全信息，例如，配置安全登录页，登录处理URL，登出页等。

注意：在ResourceServerConfiguration和WebSecurityConfiguration两个类同时存在的时候，WebSecurityConfiguration的HttpSecurity配置一定要以http.**authorizeRequests()**.antMatchers("xxx")开头来定义web授权，否则配置无效。

```java
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
```

### 2.6 application.yml

```yaml
# oauth2 Edgware 版本有bug，因此需要下面的配置把认证过滤器提前
security:
  oauth2:
    resource:
      filter-order: 3    
```



## 3.表结构

### oracle

#### OAUTH_CLIENT_DETAILS(客户端信息表)

```sql
create table OAUTH_CLIENT_DETAILS
(
  client_id               VARCHAR2(256) not null,
  resource_ids            VARCHAR2(256),
  client_secret           VARCHAR2(256), 
  scope                   VARCHAR2(256),
  authorized_grant_types  VARCHAR2(256), 
  web_server_redirect_uri VARCHAR2(256),
  authorities             VARCHAR2(256),
  access_token_validity   INTEGER,
  refresh_token_validity  INTEGER,
  additional_information  VARCHAR2(3072),
  autoapprove             VARCHAR2(256)
);
alter table OAUTH_CLIENT_DETAILS
  add constraint PK_OAUTH_CLIENT_DETAILS primary key (CLIENT_ID);
```

字段说明：

**client_id** 客户端ID

**resource_ids** 允许访问的资源服务器ID(多个用逗号分隔)，资源服务器上可以通过，如下设置：

```java
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId("oauth-server");
	}
```

如果资源服务器(ResourceServerConfiguration)上配置了resourceId，而你的客户端OAUTH_CLIENT_DETAILS.resource_ids字段没有设置相关的值，则无权访问这个资源服务器。

**client_secret** 客户端秘钥，为了安全起见，应该是一个加密值。其对于如下配置的加密器：

这是类AuthorizationServerConfiguration内的一个方法，其设置了client的加密器，其会对http请求的client_secret加密，然后和数据库上的这个字段进行比较。

```java
@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.passwordEncoder(this.passwordEncoder());
	}
```

**scope**  范围(多个用逗号分隔，例如：web,service,mobile)，自定义字符串，定义允许的范围。

**authorized_grant_types**  允许的授权模式(例如：password,authorization_code,implicit,client_credentials,refresh_token)，除了refresh_token是特殊模式外，其它的是oauth2常用的四种模式。

**web_server_redirect_uri** 在code_authorization模式下的登录成功后，应用回调url，必须和/oauth/authorize请求的请求参数redirect_uri相同，一般为http://应用ip:应用port/login，例如：http://192.168.5.32:6002/login。

**authorities** 客户端授权，只要在implicit,client_credentials模式下才有意义，因为在password和authorization_code模式下使用的user的授权。

**access_token_validity** 设定客户端的access_token的有效时间值(单位:秒),可选, 若不设定值则使用默认的有效时间值(60 * 60 * 12, 12小时)。

**refresh_token_validity** 设定客户端的refresh_token的有效时间值(单位:秒),可选, 若不设定值则使用默认的有效时间值(60 * 60 * 24 * 30, 30天)。

**additional_information** 这是一个预留的字段,在Oauth的流程中没有实际的使用,可选,但若设置值,必须是JSON格式的数据,例如：

```json
{"country":"CN","country_code":"086"}
```

**autoapprove** 设置用户是否自动Approval操作, 默认值为 'false', 可选值包括 'true','false', 'read','write'.
该字段只适用于grant_type="authorization_code"的情况,当用户登录成功后,若该值为'true'或支持的scope值,则会跳过用户Approve的页面, 直接授权。

#### OAUTH_USER(用户信息表)

```sql
-- Create table
create table OAUTH_USER
(
  user_id  NUMBER not null,
  username VARCHAR2(45) not null,
  password VARCHAR2(256) not null,
  enabled  CHAR(1) default '1'
);
alter table OAUTH_USER
  add constraint PK_OAUTH_USER primary key (USER_ID);
```

user_id 用户id

username 用户名

password 密码

enabled 是否允许，1允许，0不允许

#### OAUTH_AUTHORITY(授权表)

```sql
create table OAUTH_AUTHORITY
(
  authority_id NUMBER not null,
  name         VARCHAR2(100)
);
alter table OAUTH_AUTHORITY
  add constraint PK_OAUTH_ROLE_ID primary key (AUTHORITY_ID);
```

authority_id 授权id

name 授权名

#### OAUTH_USER_AUTHORITY(用户授权表)

OAUTH_USER和OAUTH_AUTHORITY的多对多中间表

```sql
create table OAUTH_USER_AUTHORITY
(
  user_id      NUMBER not null,
  authority_id NUMBER not null
);
alter table OAUTH_USER_AUTHORITY
  add constraint PK_OAUTH_USER_ROLE primary key (USER_ID, AUTHORITY_ID);
```

user_id 用户id

authority_id 授权id

#### 其它表

http://www.andaily.com/spring-oauth-server/db_table_description.html

## 4./oauth2/* 相关URL介绍

### /oauth2/token

作用：获取访问令牌。

**密码模式授权(password model)：**

请求URL：/oauth2/token

请求方法：POST

请求Content：application/x-www-form-urlencoded

请求参数：

grant_type = password

username = 用户名（对应OAUTH_USER.USERNAME）

password = 密码（对应OAUTH_USER.PASSWORD）

scope = 范围（对应OAUTH_CLIENT_DETAILS.SCOPE）

请求头：

Authorization Basic client_id:client_secret

这里的 client_id:client_secret 需要使用base64编码

返回值：

```json
{"access_token":"ca365461-23a5-4abb-b7d6-7f29d46976be","token_type":"bearer","refresh_token":"06909beb-b5a1-47f8-b147-ff3c4bb52aae","expires_in":43199,"scope":"service"}
```

access_token 返回的访问令牌。

token_type 令牌类型 bearer。

refresh_token 刷新令牌，允许在access_token在快要到期的时候，使用refresh_token来刷新access_token。

expires_in 过期时间。

scope 范围，一般对于请求范围，例如：请求scope=service，返回的也是service。

### /oauth2/check_token

作用：检查token是否合法。

请求URL：/oauth2/check_token

请求方法：GET

请求Content：application/x-www-form-urlencoded

请求参数：token={access_token}

请求头：

Authorization Basic client_id:client_secret

这里的 client_id:client_secret 需要使用base64编码

返回值：

```json
{"exp":1579201673,"user_name":"test_user","authorities":["test_admin","test_user"],"client_id":"test_client","scope":["service"]}
```

exp 过期时间(毫秒)

user_name 用户名

authorities 授权列表

client_id 客户端id

scope 返回列表

### /oauth2/token_key

用于JWT，目前没有用到。

### /oauth/authorize

code_authorization认证模式，使用的URL，客户端认证URL。

参见：”sc-oauth2-codeauthorization“项目的README.md说明。

### /oauth/confirm_access

code_authorization认证模式，使用的URL，用户确认URL。

参见：”sc-oauth2-codeauthorization“项目的README.md说明。

### /oauth/error

作用：oauth2的错误处理URL。