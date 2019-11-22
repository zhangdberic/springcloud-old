# 集中API(swagger2+zuul整合)

如果部署几百个服务实例(jvm)，那么需要一个可以集中展现API的方式。

就像zuul在服务网关中的作用一样（前置接入和路由转发），这里也使用zuul来前置swagger2_ui的访问，对不同服务的API文档访问，路由到不同的服务实例上。

理解为：每个服务实例上的/v2/api-docs(swagger2)也是一个服务，服务启动时把ip:port注册到eureka上，其提供的/v1/api-docs也可以通过服务客户端访问，也是通过zuul前置和转发。

当访问zuul的swagger_ui.html，会出现下拉框选择要查看服务实例。

![](https://github.com/zhangdberic/springcloud/blob/master/sw-zuul-swagger-test/doc/swagger_ui_zuul.jpg)

我们已经有一个swagger2声明的服务（sc-swagger-test)，这里我们重点介绍sc-zuul-swagger-test。

因为要转发**/{服务名}/v2/api-docs**到不同的服务实例上，因此我们要重新实现接口：SwaggerResourcesProvider

```java
@Component
@Primary
public class DocumentationConfig implements SwaggerResourcesProvider {
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(DocumentationConfig.class);
	/** zuul 路由定位器 */
    private final RouteLocator routeLocator;
    
    /**
     * spring context 自动注射 RouteLocator
     * @param routeLocator
     */
    public DocumentationConfig(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        // zuul从eureka中获取服务位置，并注册到resources中
        List<Route> routes = this.routeLocator.getRoutes();
        logger.info(Arrays.toString(routes.toArray()));
        routes.forEach(route -> {
            resources.add(swaggerResource(route.getId(), route.getFullPath().replace("**", "v2/api-docs"),"1.0"));
        });
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }
}
```

核心是RouteLocator routeLocator对象，其是zuul提供的对象，作用：**服务定位器**，List<Route> routes = this.routeLocator.getRoutes();可以获取到服务的位置，其内部是基于ribbon实现，定时从eureka上拉取路由表。

# 安全问题

应单点部署一套测试环境(spring cloud)，并提供zuul swagger访问，不应在生产环境上直接提供zuul swagger的访问。

## 公司内部访问

可以基于security basic认证，只有正确的输入用户和密码就可以访问。

```yml
# 开启安全认证(访问swagger需要basic认证)    
security:
  basic:
    enabled: true
  user:
    name: sc-zuul-swagger
    password: veDSJeUX-Jf3kWrk  
```



## 公司外部访问

可以基于spring oauth2安全体系，使用角色和登录用户来限制，每个服务实例内部基于api角色来限制访问，例如：role_api，role_view_api，role_api可以执行try it out，而role_view_api只能查看。

