package com.sc.oauth2.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 属性配置
 * @author Administrator
 *
 */
@Component
public class PropertiesConfiguration {
	/** access_token有效时间，默认12个小时 */
	@Value("${security.oauth2.authorization.tokenService.accessTokenValiditySeconds:43200}")
	private int accessTokenValiditySeconds;
	/** refresh_token有效时间，默认30天 */
	@Value("${security.oauth2.authorization.tokenService.refreshTokenValiditySeconds:2592000}")
	private int refreshTokenValiditySeconds;
	/** 是否支持刷新token，默认不支持 */
	@Value("${security.oauth2.authorization.tokenService.supportRefreshToken:true}")
	private boolean supportRefreshToken;
	/** 每次刷新token是否改变，默认改变 */
	@Value("${security.oauth2.authorization.tokenService.reuseRefreshToken:true}")
	private boolean reuseRefreshToken = true;

	public int getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	public void setAccessTokenValiditySeconds(int accessTokenValiditySeconds) {
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
	}

	public int getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	public void setRefreshTokenValiditySeconds(int refreshTokenValiditySeconds) {
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	public boolean isSupportRefreshToken() {
		return supportRefreshToken;
	}

	public void setSupportRefreshToken(boolean supportRefreshToken) {
		this.supportRefreshToken = supportRefreshToken;
	}

	public boolean isReuseRefreshToken() {
		return reuseRefreshToken;
	}

	public void setReuseRefreshToken(boolean reuseRefreshToken) {
		this.reuseRefreshToken = reuseRefreshToken;
	}

}
