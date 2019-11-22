# 集中API(swagger2+zuul整合)

如果部署几百个服务实例(jvm)，那么需要一个可以集中展现API的方式。

就像zuul在服务网关中的作用一样（前置接入和路由转发），这里也使用zuul来前置swagger2_ui的访问，对不同服务的API文档访问，路由到不同的服务实例上。

理解为：每个服务实例上的/v2/api-docs(swagger2)也是一个服务，服务启动时把ip:port注册到eureka上，其提供的/v1/api-docs也可以通过服务客户端访问，也是通过zuul前置和转发。

当访问zuul的swagger_ui.html，会出现下拉框选择要查看服务实例。

![]()

我们已经有一个swagger2声明的服务（sc-swagger-test)，这里我们重点介绍sc-zuul-swagger-test。

