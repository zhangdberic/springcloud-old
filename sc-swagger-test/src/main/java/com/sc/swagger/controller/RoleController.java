package com.sc.swagger.controller;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sc.swagger.domain.Role;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "角色相关接口", description = "提供角色相关的 Rest API")
@RestController
@RequestMapping("/roles")
public class RoleController {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

	/** 内存存储role对象,测试用 */
	private static final ConcurrentMap<Long, Role> roles = new ConcurrentHashMap<Long, Role>();
	
	
	@ApiOperation(value = "获取角色列表", notes = "获取所有的角色信息")
	@RequestMapping(method = RequestMethod.GET)
	public Collection<Role> getRoles() {
		logger.info("获取[{}]条用户信息", roles.values().size());
		return roles.values();
	}

}
