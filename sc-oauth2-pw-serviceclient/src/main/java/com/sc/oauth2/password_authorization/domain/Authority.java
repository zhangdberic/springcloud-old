package com.sc.oauth2.password_authorization.domain;

import org.springframework.security.core.GrantedAuthority;

/**
 * 角色实体
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class Authority implements GrantedAuthority {

	private Long id;

	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAuthority() {
		return name;
	}

	@Override
	public String toString() {
		return "Authority{" + "id=" + id + ", name='" + name + '\'' + '}';
	}
}
