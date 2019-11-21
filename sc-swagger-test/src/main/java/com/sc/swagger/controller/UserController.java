package com.sc.swagger.controller;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sc.swagger.domain.RetVal;
import com.sc.swagger.domain.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "用户相关接口", description = "提供用户相关的 Rest API")
@RestController
@RequestMapping("/users")
public class UserController {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	/** 内存存储user对象,测试用 */
	private static final ConcurrentMap<Long, User> users = new ConcurrentHashMap<Long, User>();

	@ApiOperation(value = "获取用户列表", notes = "获取所有的用户信息")
	@RequestMapping(method = RequestMethod.GET)
	public Collection<User> getUsers() {
		logger.info("获取[{}]条用户信息", users.values().size());
		return users.values();
	}

	@ApiOperation(value = "创建用户", notes = "根据User对象创建用户")
	@ApiImplicitParam(name = "user", value = "用户描述实体", required = true, dataType = "User")
	@RequestMapping(method = RequestMethod.POST)
	public RetVal<User> postUser(@RequestBody User user) {
		logger.info("创建用户[{}]", user);
		users.put(user.getId(), user);
		return new RetVal<User>("200", "成功", user);
	}

	@ApiOperation(value = "获取某个用户信息", notes = "根据url的id来获取用户详细信息")
	@ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", example = "1")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public User getUser(@PathVariable long id) {
		User user = users.get(id);
		logger.info("根据user.id[{}],获取到用户[{}]", id, user);
		return user;
	}

	@ApiOperation(value = "更新用户详细信息", notes = "根据url的id来指定更新对象，并根据传过来的user信息来更新用户详细信息")
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", example = "1"),
			@ApiImplicitParam(name = "user", value = "用户详细实体user", required = true, dataType = "User") })
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public RetVal<Long> putUser(@PathVariable Long id, @RequestBody User user) {
		User u = users.get(id);
		u.setName(user.getName());
		u.setAge(user.getAge());
		users.put(id, u);
		logger.info("修改user.id[{}],用户[{}]", id, user);
		return new RetVal<Long>("200", "修改成功", id);
	}

	@ApiOperation(value = "删除用户", notes = "根据url的id来指定删除对象")
	@ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", example = "1")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public RetVal<Long> deleteUser(@PathVariable Long id) {
		logger.info("删除id[{}]的用户", id);
		users.remove(id);
		return new RetVal<Long>("200", "删除成功", id);
	}

}
