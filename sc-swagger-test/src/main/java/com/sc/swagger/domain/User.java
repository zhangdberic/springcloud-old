package com.sc.swagger.domain;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "用户信息")
public class User {

	public User() {
		super();
	}

	public User(Long id, String username, String name, Integer age, BigDecimal balance) {
		super();
		this.id = id;
		this.username = username;
		this.name = name;
		this.age = age;
		this.balance = balance;
	}

	@ApiModelProperty(value = "用户id", required = true, example = "1")
	private Long id;
	@ApiModelProperty(value = "登录用户名", required = true, example = "heige")
	private String username;
	@ApiModelProperty(value = "用户名称", required = true, example = "黑哥")
	private String name;
	@ApiModelProperty(value = "年龄", example = "10", allowableValues = "range[1, 150]")
	private Integer age;
	@ApiModelProperty(value = "结余", example = "423.05", allowableValues = "range[0.00, 99999999.99]")
	private BigDecimal balance;

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", name=" + name + ", age=" + age + ", balance=" + balance + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

}
