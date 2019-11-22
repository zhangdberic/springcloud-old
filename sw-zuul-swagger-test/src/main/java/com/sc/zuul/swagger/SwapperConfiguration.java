package com.sc.zuul.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwapperConfiguration {
	
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo());
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Zuul构建Swagger2 RESTful APIs入口")
                .description("更多Spring Cloud相关文章请关注：https://github.com/zhangdberic/springcloud/")
                .termsOfServiceUrl("https://github.com/zhangdberic/")
                .contact(new Contact("zhangdberic","https://github.com/zhangdberic","909933699@qq.com"))
                .version("1.0")
                .build();
    }
	
}
