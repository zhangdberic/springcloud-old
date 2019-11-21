# swagger2 RESTful API文档

它可以轻松的整合到Spring Boot中，并与Spring MVC程序配合组织出强大RESTful API文档。它既可以减少我们创建文档的工作量，同时说明内容又整合入实现代码中，让维护文档和修改代码整合为一体，可以让我们在修改代码逻辑的同时方便的修改文档说明。另外Swagger2也提供了强大的页面测试功能来调试每个RESTful API。



# 查看swagger生成的API文档

 http://localhost:8080/swagger-ui.htm 

![](https://github.com/zhangdberic/springcloud/blob/master/sc-swagger-test/doc/swagger2_ui_index.jpg)

# swagger2的api介绍

### 1.建议使用独立swaggerConfiguration配置类

```java
@Configuration
@EnableSwagger2
public class SwapperConfiguration {
	
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sc.swagger"))
                .paths(PathSelectors.any())
                .build();
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Spring Boot中使用Swagger2构建RESTful APIs")
                .description("更多Spring Cloud相关文章请关注：https://github.com/zhangdberic/springcloud/")
                .termsOfServiceUrl("https://github.com/zhangdberic/")
                .contact(new Contact("zhangdberic","https://github.com/zhangdberic","909933699@qq.com"))
                .version("1.0")
                .build();
    }

}
```

**apis(RequestHandlerSelectors.basePackage("com.sc.swagger"))**，指定扫描指定包(包含子包)路径下的swapper注释类来生成api文档。

### 2.@Api

```java
@Api(tags = "用户相关接口", description = "提供用户相关的 Rest API")
@RestController
@RequestMapping("/users")
public class UserController {}
```

@Api源注释标注一个RestController，对整个RestController进行说明，例如：UserController，标注为用户相关接口。如果不使用@Api标注，则swagger-ui是显示UserController字样。

### 3.@ApiOperation

```java
	@ApiOperation(value = "获取用户列表", notes = "获取所有的用户信息")
	@RequestMapping(method = RequestMethod.GET)
	public Collection<User> getUsers() {
		logger.info("获取[{}]条用户信息", users.values().size());
		return users.values();
	}
```

@ApiOperation标注一个Controller内的方法，也就是一个操作。

### 4.@ApiImplicitParam

```java
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
```

@ApiImplicitParam标注一个参数

name 参数名

value 描述

required 是否必填,对应swagger ui上是一个红色的星号

**dataType** 指定了参数类型，注意：这里只能两种类型。

1、java基本类型，例如：string、long、int等

2、@ApiModel标注类名，例如：这里dataType="User"，User是一个domain类，其已经使用@ApiModel标注，例如dataType="User"，对应的User类@ApiModel声明：

```java
@ApiModel(description = "用户信息")
public class User {
    
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
}
```

**example** 参数示例值，如果参数是一个java基本类型，则需要指定，否则会抛出异常。如果是一个类似于上面的dataType="User"的@ApiModel声明类型，则无需声明example，因为@ApiModel标注的类，本身就会提供example。

根据上面的@ApiOperation和@ApiImplicitParam的配置，生成的swagger ui截图：

**参数部分**

注意观察，界面上的Example Value，就是User类上标注的@ApiModelProperty的example。

![](https://github.com/zhangdberic/springcloud/blob/master/sc-swagger-test/doc/swagger_ui_apilmplicitparam.jpg)

**响应部分**

注意观察，界面上的Example Value，对应RetVal<Long>，包括RetVal内的泛型都可以正确识别（根据方法声明的返回值来生成的)。

![](https://github.com/zhangdberic/springcloud/blob/master/sc-swagger-test/doc/swagger2_ui_retval.jpg)