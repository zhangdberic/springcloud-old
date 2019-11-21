package com.sc.swagger.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "角色信息")
public class Role {

	@ApiModelProperty(value = "角色id", required = true, example = "1")
	private Long id;
	@ApiModelProperty(value = "角色名", required = true, example = "admin")
	private String name;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
