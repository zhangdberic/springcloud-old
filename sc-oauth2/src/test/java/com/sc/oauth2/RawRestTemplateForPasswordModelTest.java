package com.sc.oauth2;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import z1.util.coder.Base64Coder;

/**
 * 原生的RestTemplate客户端，测试密码模式，所有的协议都需要自己实现,
 * 
 */
public class RawRestTemplateForPasswordModelTest {

	@Test
	public void test() throws Exception {
		// 测试获取access_token
		Map<String, String> authInfoMap = this.getToken();
		String accessToken = authInfoMap.get("access_token");
		// 检查access_token是否合法
		this.checkToken(accessToken);
		// 获取登录用户信息
		this.getUser(accessToken);
		// 刷新access_token
		String refreshToken = authInfoMap.get("refresh_token");
		this.refreshToken(refreshToken);
	}

	/**
	 * 获取access_token
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String> getToken() throws Exception {
		// 构造 post的body内容（要post的内容，按需定义）
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.set("grant_type", "password");
		paramsMap.set("username", OauthClientTestConfig.OAUTH_USERNAME);
		paramsMap.set("password", OauthClientTestConfig.OAUTH_PASSWORD);
		paramsMap.set("scope", OauthClientTestConfig.OAUTH_CLIENT_SERVICE_SCOPE);

		// 构造头部信息(若有需要)
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + new Base64Coder().encode(
				(OauthClientTestConfig.OAUTH_CLIENT_ID + ":" + OauthClientTestConfig.OAUTH_CLIENT_SECRET).getBytes()));
		// 设置类型 "application/json;charset=UTF-8"
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// 构造请求的实体。包含body和headers的内容
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(paramsMap,
				headers);

		// 声明 restTemplateAuth（用作请求）
		RestTemplate restTemplateAuth = new RestTemplate();
		// 发送请求，并返回数据
		String authInfo = restTemplateAuth.postForObject(OauthClientTestConfig.OAUTH_GET_TOKEN_URL, request,
				String.class);
		System.out.println(authInfo);
		
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(authInfo.getBytes(), LinkedHashMap.class);
	}

	/**
	 * 检查access_token是否合法
	 * @param accessToken
	 */
	protected void checkToken(String accessToken) {

		// 声明 restTemplateAuth（用作请求）
		RestTemplate restTemplateAuth = new RestTemplate();

		// 构造头部信息(若有需要)
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + new Base64Coder().encode(
				(OauthClientTestConfig.OAUTH_CLIENT_ID + ":" + OauthClientTestConfig.OAUTH_CLIENT_SECRET).getBytes()));
		// 设置类型 "application/json;charset=UTF-8"
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// 构造请求的实体。包含body和headers的内容
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
				headers);

		ResponseEntity<String> respEntity = restTemplateAuth.exchange(
				OauthClientTestConfig.OAUTH_CHECK_TOKEN_URL + "?token=" + accessToken, HttpMethod.GET, request,
				String.class);
		System.out.println(respEntity);

	}

	/**
	 * 获取用户信息
	 * @param accessToken
	 */
	protected void getUser(String accessToken) {
		// 声明 restTemplateAuth（用作请求）
		RestTemplate restTemplateAuth = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<String> respEntity = restTemplateAuth.exchange(OauthClientTestConfig.OAUTH_GETUSER_URL,
				HttpMethod.GET, requestEntity, String.class);
		System.out.println(respEntity);
	}

	/**
	 * 刷新token
	 * @param refreshToken
	 */
	protected void refreshToken(String refreshToken) {
		// 声明 restTemplateAuth（用作请求）
		RestTemplate restTemplateAuth = new RestTemplate();
		ResponseEntity<String> respEntity = restTemplateAuth.postForEntity(OauthClientTestConfig.OAUTH_GET_TOKEN_URL
				+ "?grant_type=refresh_token&refresh_token=" + refreshToken + "&client_id="
				+ OauthClientTestConfig.OAUTH_CLIENT_ID + "&client_secret=" + OauthClientTestConfig.OAUTH_CLIENT_SECRET,
				null, String.class);
		System.out.println(respEntity);
	}

}
