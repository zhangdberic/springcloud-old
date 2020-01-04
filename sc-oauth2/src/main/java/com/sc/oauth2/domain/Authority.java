package com.sc.oauth2.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

/**
 * 角色实体
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "oauth_authority")
public class Authority implements GrantedAuthority {

	@Id
	@Column(name = "authority_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
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
