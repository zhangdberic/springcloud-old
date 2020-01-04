package com.sc.oauth2;

/**
 * oauth2客户端测试配置
 * 生产环境则不能使用本配置应放在配置文件中
 * @author Administrator
 *
 */
public class OauthClientTestConfig {
	/** oauth服务器地址 */
	public static final String OAUTH_SERVICE_LOCATION = "http://localhost:6001";
	/** oauth获取token地址 */
	public static final String OAUTH_GET_TOKEN_URL = OAUTH_SERVICE_LOCATION + "/oauth/token";
	/** oauth检查token地址 */
	public static final String OAUTH_CHECK_TOKEN_URL = OAUTH_SERVICE_LOCATION + "/oauth/check_token";
	/** oauth获取用户信息地址 */
	public static final String OAUTH_GETUSER_URL = OAUTH_SERVICE_LOCATION + "/auth/user";
	/** 测试client_id */
	public static final String OAUTH_CLIENT_ID = "test_client";
	/** 测试client_secret */
	public static final String OAUTH_CLIENT_SECRET = "123456";
	/** 测试client_scope */
	public static final String OAUTH_CLIENT_SERVICE_SCOPE = "service";
	/** 测试client_scope */
	public static final String OAUTH_CLIENT_WEB_SCOPE = "web";
	/** 测试用户username */
	public static final String OAUTH_USERNAME = "test_user";
	/** 测试用户password */
	public static final String OAUTH_PASSWORD = "123456";

}
