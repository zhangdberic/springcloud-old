# eureka集群配置

eureka单机没有任何意义，服务注册和发现中心必须部署两个以上。

基于docker部署，测试环境基于单宿主机部署两个eureka docker来模拟eureka集群。

具体的代码，参见当前目录下github代码。

这里重点对配置进行说明：

[bootstrap.yml](https://github.com/zhangdberic/springcloud/blob/master/sc-eureka/src/main/resources/bootstrap.yml)

bootstrap.yml应该只放在当前profile、应用名称，基本的远程配置信息，eureka应用信息应放到application.yml中。

application.yml应配置到config git上，其应该由sc-eureka-default.yml和sc-eureka-{profile}.yml两个文件组成，例如开发环境：config-repo/sc-eureka/sc-eureka.yml和config-repo/sc-eureka/sc-eureka-dev.yml

[application.yml]()

```yml
server:
  port: 8070
spring:
  cloud:
    config:
      # 允许使用java -Dxxx=yyy,来覆盖远程属性，例如:java -Dserver.port=8071
      overrideSystemProperties: false
  # 和spring-cloud-starter-bus-amqp配合,用于/bus/refresh分布式服务属性刷新
  rabbitmq:
    host: 192.168.5.29
    port: 5672
    username: admin
    password: Rabbitmq-401
eureka:
  instance: 
    # 当建立eureka集群时必须使用基于主机名方式(不能直接使用ip地址),相应的要修改linux的/etc/hosts文件
    hostname: eureka1
  client:
    service-url:
      # 两个eureka组成集群
      defaultZone: http://sc-eureka:veDSJeUX-JSxkWrk@eureka1:8070/eureka/,http://sc-eureka:veDSJeUX-JSxkWrk@eureka2:8071/eureka/

```

这里重点说一下，如果是基于集群配置，应基于**主机名**方式来配置eureka，而不是ip方式，仔细看defaultZone的地址都是主机名，其在eureka.instance.hostname中声明了。在hostname中声明的主机名必须在linux的/etc/hosts中同步设置，例如：192.168.5.78 dyit.com eureka1 eureka2。



## FAQ

1、建议基于docker host网络模式来部署eureka，不应使用birdge模式，防止eureka注册的ip为172.17.0.x、防止docker主机名重启变动(这些都是birdge模式的特殊性造成的)。



## DOCKER RUN脚本

docker run -itd --cap-add=SYS_PTRACE --name sc-eureka1 --net host -e JAVA_OPTS="-Xms200m -Xmx200m -Xmn80m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev --server.port=8070 --eureka.instance.hostname=eureka1"  dyit.com:5000/sc/sc-eureka:1.0.1

docker run -itd --cap-add=SYS_PTRACE --name sc-eureka2 --net host -e JAVA_OPTS="-Xms200m -Xmx200m -Xmn80m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev --server.port=8071 --eureka.instance.hostname=eureka2"  dyit.com:5000/sc/sc-eureka:1.0.1

注意：

1.基于--net host网络模式启动，见FAQ。

2.--server.port指定了端口，两个eureka对应两个不同的端口。

3.--eureka.instance.hostname指定了主机名，两个eureka对应两个不同的主机名，这里的主机名必须在/etc/hosts中配置。



## 运行成功截图

![eureka1](https://github.com/zhangdberic/springcloud/blob/master/sc-eureka/doc/eureka-ha2.png)



