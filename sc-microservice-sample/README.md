# 服务提供者测试例子



其会在启动的时候通过config来获取git上的配置 [sc-sampleservice-dev.yml]( https://github.com/zhangdberic/config-repo/blob/master/sc-sampleservice/sc-sampleservice-dev.yml )，并注册sc-sampleservcie服务到eureka上。



提供了一个测试URL:http://192.168.5.78:8000/1，查看用户id为1的用户信息。



http://192.168.5.78:8000/health，监控检查可以查看到：db、rabbitmq、config、eureka、hystrix等状态，比较全。



在测试环境上，可以部署两个sc-sampleservice服务。用于测试如下：

1.eureka上是否可以正确的识别两个服务实例。可以通过eureka主页面来验证，http://192.168.5.78:8070/

2.sc-sampleservice服务提供者角色，供ribbon测试负载均衡请求，sc-ribbon-test服务对sc-sampleservice服务发起请求，两个sc-sampleservice服务实例轮番被请求到。



## docker启动脚本

docker run -itd --cap-add=SYS_PTRACE --name sc-microservice-sample1 --net host -e JAVA_OPTS="-Xms100m -Xmx100m -Xmn60m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev --server.port=8000" dyit.com:5000/sc/sc-microservice-sample:1.0.1

docker run -itd --cap-add=SYS_PTRACE --name sc-microservice-sample2 --net host -e JAVA_OPTS="-Xms100m -Xmx100m -Xmn60m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev --server.port=8001" dyit.com:5000/sc/sc-microservice-sample:1.0.1



