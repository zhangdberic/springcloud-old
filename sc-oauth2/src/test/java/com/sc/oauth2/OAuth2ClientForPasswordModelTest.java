package com.sc.oauth2;

/**
 * spring security提供的OAuth2Client，测试密码模式
 * @author Administrator
 *
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试spring security提供的OAuth2RestTemplate
 * @author Administrator
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OAuthClientConfiguration.class)
public class OAuth2ClientForPasswordModelTest {

	@Autowired
	private OAuth2RestTemplate oauth2RestTemplate;

	@Test
	public void test() {
		// 获取access_token
		OAuth2AccessToken oauth2AccessToken = this.oauth2RestTemplate.getAccessToken();
		String accessToken = oauth2AccessToken.getValue();
		System.out.println(accessToken);
		// 检查access_token
		// OAuth2RestTemplate没有提供的特殊的方法,参见RawRestTemplateForPasswordModelTest.checkToken(String accessToken);
		// 获取用户
		// OAuth2RestTemplate没有提供的特殊的方法,参见RawRestTemplateForPasswordModelTest.getUser(String accessToken);
		// 刷新token
		// OAuth2RestTemplate没有提供的特殊的方法,参见RawRestTemplateForPasswordModelTest.refreshToken(String refreshToken);
	}

}
