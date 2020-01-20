# oauth2 代码认证模式(code_authorization)

## 1.理论

**授权码（authorization code）方式，指的是第三方应用先申请一个授权码，然后再用该码获取令牌。**

这种方式是最常用的流程，安全性也最高，它适用于那些有后端的 Web 应用。授权码通过前端传送，令牌则是储存在后端，而且所有与资源服务器的通信都在后端完成。这样的前后端分离，可以避免令牌泄漏。

第一步，A 网站提供一个链接，用户点击后就会跳转到 B 网站，授权用户数据给 A 网站使用。下面就是 A 网站跳转 B 网站的一个示意链接。

> ```javascript
> https://b.com/oauth/authorize?
>   response_type=code&
>   client_id=CLIENT_ID&
>   redirect_uri=CALLBACK_URL&
>   scope=read
> ```

上面 URL 中，`response_type`参数表示要求返回授权码（`code`），`client_id`参数让 B 知道是谁在请求，`redirect_uri`参数是 B 接受或拒绝请求后的跳转网址，`scope`参数表示要求的授权范围（这里是只读）。

![img](https://www.wangbase.com/blogimg/asset/201904/bg2019040902.jpg)

第二步，用户跳转后，B 网站会要求用户登录（输入用户名和密码），然后询问是否同意给予 A 网站授权。用户表示同意，这时 B 网站就会跳回`redirect_uri`参数指定的网址。跳转时，会传回一个授权码，就像下面这样。

> ```javascript
> https://a.com/callback?code=AUTHORIZATION_CODE
> ```

上面 URL 中，`code`参数就是授权码。

![img](https://www.wangbase.com/blogimg/asset/201904/bg2019040907.jpg)

第三步，A 网站拿到授权码以后，就可以在后端，向 B 网站请求令牌。

> ```javascript
> https://b.com/oauth/token?
>  client_id=CLIENT_ID&
>  client_secret=CLIENT_SECRET&
>  grant_type=authorization_code&
>  code=AUTHORIZATION_CODE&
>  redirect_uri=CALLBACK_URL
> ```

上面 URL 中，`client_id`参数和`client_secret`参数用来让 B 确认 A 的身份（`client_secret`参数是保密的，因此只能在后端发请求），`grant_type`参数的值是`AUTHORIZATION_CODE`，表示采用的授权方式是授权码，`code`参数是上一步拿到的授权码，`redirect_uri`参数是令牌颁发后的回调网址。

![img](https://www.wangbase.com/blogimg/asset/201904/bg2019040904.jpg)

第四步，B 网站收到请求以后，就会颁发令牌。具体做法是向`redirect_uri`指定的网址，发送一段 JSON 数据。

> ```javascript
> {    
>   "access_token":"ACCESS_TOKEN",
>   "token_type":"bearer",
>   "expires_in":2592000,
>   "refresh_token":"REFRESH_TOKEN",
>   "scope":"read",
>   "uid":100101,
>   "info":{...}
> }
> ```

上面 JSON 数据中，`access_token`字段就是令牌，A 网站在后端拿到了。

![img](https://www.wangbase.com/blogimg/asset/201904/bg2019040905.jpg)

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

### 2.2 CodeAuthorizationApplication

```java
@SpringBootApplication
public class CodeAuthorizationApplication {	

	public static void main(String[] args) {
		SpringApplication.run(CodeAuthorizationApplication.class, args);
	}
}
```

没有什么特殊的源注释(配置)，特殊的源注释在其它security相关类上声明。

### 2.3 application.yml

```yaml
server:
  session:
    cookie:
      # 重命名session.cookie名称,防止覆盖oauth server的JSESSIONID
      name: testclient
      
# oauth2 代码模式(code_authorization)客户端配置
oauth2-server: http://localhost:6001
security:
  oauth2:
    client:
      grant-type: code_credentials    # 代码授权模式
      client-id: test_client        # 在oauth 服务端注册的client-id
      client-secret: 123456     # 在oauth 服务端注册的secret
      access-token-uri: ${oauth2-server}/oauth/token    #获取token 地址
      user-authorization-uri: ${oauth2-server}/oauth/authorize  # 认证地址
      scope: web
    resource:
      token-info-uri: ${oauth2-server}/oauth/check_token  # 检查token
      user-info-uri: ${oauth2-server}/auth/user   # 用户信息
    sso:
      login-path: /login  # 回调登录页      
```

server.session.cookie.name=testclient

oauth2的客户端，必须重新设置session的cookie名称，否则在同一域名内会相互覆盖。因为默认spring boot的默认session cookie名称为JSESSIONID，而你为了完成单点登录需要三个项目，oauth2服务器，oauth2客户端1，oauth2客户端2，如果三个项目都使用JSESSION的cookie名称，并且在同一关域内（例如：测试为location），那么三个项目的session cookie会相互覆盖，因此必须为两个测试oauth2客户端重命名session的cookie名称。

问题：目前基于code_authorization模式下，还不能实现基于eureka方式来发现oauth server服务(/oauth2/*)，只能使用oauth2-server： http://localhost:6001，方式来声明一个配置文件变量的方式，尽量来减少将来的oauth2 server服务位置修改带来的影响。这个问题待以后解决。

### 2.4 oauth2 server配置

#### 2.4.1 代码认证模式下的client_details

oauth2 服务器端，要配置允许这个第三方应用访问oauth2服务器。

| 字段名                      | 数据(样例)                                                   |
| --------------------------- | ------------------------------------------------------------ |
| CLIENT_ID                   | test_client                                                  |
| RESOURCE_IDS                |                                                              |
| CLIENT_SECRET               | {bcrypt}$2a$10$uT8xtlOWnIiS9Es1QVN9LeKcWpoeuk.bZqgFpNVsCFWacuXn/Moei |
| SCOPE                       | service,web                                                  |
| **AUTHORIZED_GRANT_TYPES**  | refresh_token,password,authorization_code                    |
| **WEB_SERVER_REDIRECT_URI** | http://localhost:6002/login                                  |
| AUTHORITIES                 |                                                              |
| ACCESS_TOKEN_VALIDITY       |                                                              |
| REFRESH_TOKEN_VALIDITY      |                                                              |
| ADDITIONAL_INFORMATION      |                                                              |
| **AUTOAPPROVE**             |                                                              |

code_authorization模型下，有三个字段会被使用：

**AUTHORIZED_GRANT_TYPES**，必须含有authorization_code字样。

**WEB_SERVER_REDIRECT_URI**，认证成功后重定向到应用的URL，必须和/oauth/authorize请求的请求参数redirect_uri相同，一般为http://应用ip:应用port/login，例如：http://192.168.5.32:6002/login，客户端可以通过修改security.oauth2.login-path=/login来配置回调URL。

**AUTOAPPROVE**，是否自动授权（是否需要用户手工点击授权），字段值为true或者与请求参数scope值相同时，则不需要用户手工授权，变为自动授权。默认为NULL的时候，需要用户手工授权。

#### 2.4.2 oauth_user

oauth2_user表加入允许登录的用户。

### 2.5 SecurityConfiguration类

@EnableWebSecurity 开启web安全，自动创建和配置spring security的bean；

@EnableOAuth2Sso 开启单点登录的sso模式，自动和配置spring security的bean的cient；

```java
@Configuration
@EnableWebSecurity
@EnableOAuth2Sso
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

上面的configure(HttpSecurity http)方法内配置规则：

1. /login无须授权就可以访问。

2. 其他的url必须认证通过后才可以方法。

## 3.测试

### 3.1 页面测试

启动三个项目：

sc-oauth2（认证服务器），端口6001

sc-oauth2-codeauthorization（第三方应用1），端口6002

sc-oauth2-codeauthorization1（第三方应用2），端口6003

发送请求：http://localhost:6002/

发现还没登录，则会自动重定向到http://localhost:6001/login，正确输入User和Password，点击登录；

登录成功后，重定向回http://localhost:6002/，显示 ”客户端主页(6002)-测试成功“。

再发送请求：http://localhost:6003/，则无需登录（因为上面已经登录)，直接显示 ”客户端主页(6003)-测试成功“。

### 3.2 /oauth2/* 

结合3.1 的页面测试，把这个认证请求的全过程说明一下，重点说明请求的URL：

发送请求http://localhost:6002/到sc-oauth2-codeauthorization服务器，

sc-oauth2-codeauthorization验证还没登录，则生成URL：/oauth/authorize?**（例如：http://localhost:6001/oauth/authorize?client_id=test_client&redirect_uri=http://localhost:6002/login&response_type=code&scope=web&state=dj43AZ），并重定向到sc-oauth2。

sc-oauth2收到/oauth/authorize?**请求，会进入登录页（http://localhost:6001/login）,提示最终用户输入用户名和密码。

最终用户输入用户名和密码提交到http://localhost:6001/login，其验证成功后，会重定向会sc-oauth2-codeauthorization的/login?**，并带上code和state两个参数（例如：http://localhost:6002/login?code=NNYMgO&state=dj43AZ）。

sc-oauth2-codeauthorization根据code生成URL：/oauth2/token（例如：http://localhost:6001/oauth/token，/oauth2/token的请求介绍见：sc-auth2文档），并使用RestTemplate发送这个请求到sc-oauth2来获取access_token。

sc-oauth2-codeauthorization获取access_token成功后，会重定向回最初发送请求的url（例如：http://localhost:6002/），因为已经登录了，就可以顺利的进入sc-oauth2-codeauthorization的"/"，页面了。

#### /oauth2/authorize

作用：客户端认证，客户端认证成功后，其会跳转到oauth server的登录页(/login)。

例如：

http://localhost:6001/oauth/authorize?client_id=test_client&redirect_uri=http://localhost:6002/login&response_type=code&scope=web&state=dj43AZ

client_id 请求客户端id

redirect_url 回调应用的url

response_type 响应类型，固定为code

scope 客户端范围

state 状态码，请求时自定义一个随机码，用户登录成功后，重定向回到redirect_uri时，会带上这个参数，为了安全。例如：登录成功后(用户名和密码输入正确后)，回调的URL如下：http://localhost:6002/login?code=NNYMgO&state=dj43AZ

### 3.3 cookie

按照上面的页面测试，开启了三个项目，如果按照上面的步骤，会生成三个cookie，oauth2 server的JSESSIONID的cookie(会话)、sc-oauth2-codeauthorization的testclient的cookie(会话)、sc-oauth2-codeauthorization1的testclient1的cookie(会话)。其中testclient和testclient1，是两个sc-oauth2-codeauthorization项目在配置server.session.cookie.name中指定的。同时，这三个cookie也是维系整个oauth2登录体系的令牌。

## 4.logout登出

### 4.1 单点登出

**第三方应用配置登出**

在第三方应用（例如：sc-oauth2-codeauthorization）的SecurityConfiguration类配置，配置登出成功url，其在调用本应用成功后会调用oauth2服务器端url，完成oauth server的登出。

```java
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
```

这里的URL：http://localhost:6001/auth/exit?clientId="+this.clientId，会发送登出请求到oauth2 server。

**oauth server 登出处理**

```java
	@Autowired 
	private ClientDetailsService clientDetailsService;

	@RequestMapping(value="/auth/exit",params="clientId")
	public void exit(HttpServletRequest request, HttpServletResponse response,@RequestParam String clientId) {
		ClientDetails clientDetails = this.clientDetailsService.loadClientByClientId(clientId);
		if(clientDetails!=null && !CollectionUtils.isEmpty(clientDetails.getRegisteredRedirectUri())) {
			// oauth server 登出
			new SecurityContextLogoutHandler().logout(request, null, null);
			// 使用在client_details注册回调uri中最后一个作为退出回调uri
			String[] clientRedirectUris = clientDetails.getRegisteredRedirectUri().toArray(new String[0]);
			String appRedirectUrl = clientRedirectUris[clientRedirectUris.length-1];
			try {
				response.sendRedirect(appRedirectUrl);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

	}
```

通过执行spring security的new SecurityContextLogoutHandler().logout(request, null, null)，完成服务器端登出(session invalid)。通过clientId请求参数获取client_detials的回调url，重定向到发起请求的client端。



