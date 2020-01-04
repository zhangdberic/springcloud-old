package com.sc.oauth2.authorization;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * @author Administrator
 *
 */
@RestController
public class UserController {
	/**
	 * 根据请求头Authorization的值来获取UserDetails信息
	 * 例如：Authorization=Bearer c53ff76b-0e9d-4dc1-9ebf-3d8acc89506f
	 * @param user
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/auth/user" }, produces = "application/json")
	public Map<String, Object> user(OAuth2Authentication user, HttpServletRequest request) {
		Map<String, Object> userinfo = new HashMap<>();
		userinfo.put("user", user.getUserAuthentication().getPrincipal());
		userinfo.put("authorities", AuthorityUtils.authorityListToSet(user.getUserAuthentication().getAuthorities()));
		return userinfo;
	}
}
