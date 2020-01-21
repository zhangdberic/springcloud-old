# 密码模式(password authorization)客户端

## 1.理论

**如果你高度信任某个应用，RFC 6749 也允许用户把用户名和密码，直接告诉该应用。该应用就使用你的密码，申请令牌，这种方式称为"密码式"（password）。**

第一步，A 网站要求用户提供 B 网站的用户名和密码。拿到以后，A 就直接向 B 请求令牌。

> ```javascript
> https://oauth.b.com/token?
>   grant_type=password&
>   username=USERNAME&
>   password=PASSWORD&
>   client_id=CLIENT_ID
> ```

上面 URL 中，`grant_type`参数是授权方式，这里的`password`表示"密码式"，`username`和`password`是 B 的用户名和密码。

第二步，B 网站验证身份通过后，直接给出令牌。注意，这时不需要跳转，而是把令牌放在 JSON 数据里面，作为 HTTP 回应，A 因此拿到令牌。

这种方式需要用户给出自己的用户名/密码，显然风险很大，因此只适用于其他授权方式都无法采用的情况，而且必须是用户高度信任的应用。

## 2.配置

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
```

### 2.2 PasswordAuthorizationApplication

```java
@SpringBootApplication
public class PasswordAuthorizationApplication {

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
				HttpServletRequest httpRequest = requestAttributes.getRequest();
				request.getHeaders().add("Authorization", httpRequest.getHeader("Authorization"));
				return execution.execute(request, body);
			}});
		return restTemplate;
	}



	public static void main(String[] args) {
		SpringApplication.run(PasswordAuthorizationApplication.class, args);
	}
}
```

这里唯一需要注意的就是RestTemplate的spring bean，其内部添加了一个ClientHttpRequestInterceptor，目的是把请求的Authorization头，在服务请求之间传递。目前看在Password认证模式下，这个一个好的解决方案了，因为OAuth2RestTemplate只支持authorization_code和client_credentials模式，不支持password和implicit。

### 2.3 application.yml

```yaml
# oauth2 security
security:
  oauth2:
    resource:
      #userInfoUri: http://localhost:6001/auth/user
      loadBalanced: true
      userInfoUri: http://SC-OAUTH2/auth/user
```

### 2.4 oauth2 server配置

#### 2.4.1 代码认证模式下的client_details

oauth2 服务器端，要配置允许这个第三方应用访问oauth2服务器。

| 字段名                     | 数据(样例)                                                   |
| -------------------------- | ------------------------------------------------------------ |
| CLIENT_ID                  | test_client                                                  |
| RESOURCE_IDS               |                                                              |
| CLIENT_SECRET              | {bcrypt}$2a$10$uT8xtlOWnIiS9Es1QVN9LeKcWpoeuk.bZqgFpNVsCFWacuXn/Moei |
| SCOPE                      | service,web                                                  |
| **AUTHORIZED_GRANT_TYPES** | refresh_token,password,authorization_code                    |
| WEB_SERVER_REDIRECT_URI    |                                                              |
| AUTHORITIES                |                                                              |
| ACCESS_TOKEN_VALIDITY      |                                                              |
| REFRESH_TOKEN_VALIDITY     |                                                              |
| ADDITIONAL_INFORMATION     |                                                              |
| AUTOAPPROVE                |                                                              |

code_authorization模型下，有三个字段会被使用：

**AUTHORIZED_GRANT_TYPES**，必须含有password字样。

#### 2.4.2 oauth_user

oauth2_user表加入允许登录的用户。

### 2.5 ResourceServerConfiguration

注意：类头源注释@EnableResourceServer，其会创建spring security相关bean。

configure(HttpSecurity http)方法内部配置的规则，是/pw/**的请求必须是已经认证（已经登录）。

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
		http.antMatcher("/pw/**").authorizeRequests().anyRequest().authenticated();
	}
}
```

### 

## 3.测试

### 3.1 启动相关项目

sc-oauth2   oauth2 服务器，端6001

sc-oauth2-pw-serviceclient  密码模式测试客户端1，端口6005

sc-oauth2-pw-serviceclient1  密码模式测试客户端2，端口6006

### 3.2 发送请求到oauth2 server

**发送请求到oauth2 server获取access_token**

curl -X POST -H 'Content-Type: application/x-www-form-urlencoded' -H 'Authorization: Basic dGVzdF9jbGllbnQ6MTIzNDU2' -i http://localhost:6001/oauth/token --data 'grant_type=password&username=test_user&password=123456&scope=service'

这里的Authorization: Basic dGVzdF9jbGllbnQ6MTIzNDU2的Basic 值是client_id:client_sercret base64后的值。

```json
{
  "access_token": "c74b23f1-dff1-4aa2-b441-381b4a502d2b",
  "token_type": "bearer",
  "refresh_token": "246cdbe1-02cf-4be1-ab68-56f6e5d01f0b",
  "expires_in": 43199,
  "scope": "service"
}
```

### 3.3 发送请求到sc-oauth2-pw-serviceclient

携带access_token，发送请求到sc-oauth2-pw-serviceclient，其内部会调用sc-oauth2-pw-serviceclient1，用于测试认证请求传递。

请求URL：

curl -X GET -H 'Authorization: Bearer c74b23f1-dff1-4aa2-b441-381b4a502d2b' -i http://localhost:6005/pw/test/1

```
success, success_pw1_test1
```

其中第一个success为sc-oauth2-pw-serviceclient的结果。

success_pw1_test1为sc-oauth2-pw-serviceclient1返回的结果（认证请求服务间传递）。

传递代码：

```java
	@Autowired
	private RestTemplate oauth2RestTemplate;
	
	@GetMapping("/pw/test/1")
	public String test1() {
		OAuth2Authentication oauth2Authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		System.out.println(oauth2Authentication);
		String pw1TestResult = this.oauth2RestTemplate.getForObject("http://SC-OAUTH2-PW-SERVICECLIENT1//pw1/test/1", String.class);
		return "success, "+pw1TestResult;
	}
```

这里的oauth2RestTemplate比较是经过处理的，如下：

```java
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
				HttpServletRequest httpRequest = requestAttributes.getRequest();
				request.getHeaders().add("Authorization", httpRequest.getHeader("Authorization"));
				return execution.execute(request, body);
			}});
		return restTemplate;
	}
```





