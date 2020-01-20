package com.sc.oauth2.authorization;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 代码模式,单点退出
 * @author zhangdb
 *
 */
@Controller
public class LogoutController {

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

}
