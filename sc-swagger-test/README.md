# swagger2 RESTful API文档

它可以轻松的整合到Spring Boot中，并与Spring MVC程序配合组织出强大RESTful API文档。它既可以减少我们创建文档的工作量，同时说明内容又整合入实现代码中，让维护文档和修改代码整合为一体，可以让我们在修改代码逻辑的同时方便的修改文档说明。另外Swagger2也提供了强大的页面测试功能来调试每个RESTful API。



# 查看swagger生成的API文档

 http://localhost:8080/swagger-ui.htm 

![](https://github.com/zhangdberic/springcloud/blob/master/sc-swagger-test/doc/swagger2_ui_index.jpg)

# swagger2的api介绍

swapper2的api设计的比较简单易懂，这里这对模糊或经常出错的api进行说明。

## 1.建议使用独立swaggerConfiguration配置类

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

[对应主页面的上部分]()

## 2.@Api

```java
@Api(tags = "用户相关接口", description = "提供用户相关的 Rest API")
@RestController
@RequestMapping("/users")
public class UserController {}
```

@Api源注释标注一个RestController，对整个RestController进行说明，例如：UserController，标注为用户相关接口。如果不使用@Api标注，则swagger-ui是显示UserController字样。





