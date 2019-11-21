package com.sc.swagger.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "响应信息")
public class RetVal<T> {
	@ApiModelProperty(value = "代码", required = true, example = "200")
	private String code;
	@ApiModelProperty(value = "信息", required = true, example = "成功")
	private String message;
	@ApiModelProperty
	private T body;

	public RetVal() {
		super();
	}

	public RetVal(String code, String message) {
		super();
		this.code = code;
		this.message = message;
	}
	
	public RetVal(String code, String message,T body) {
		super();
		this.code = code;
		this.message = message;
		this.body =  body;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}

}
