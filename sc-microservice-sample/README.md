# 服务提供者测试例子



其会在启动的时候通过config来获取git上的配置 [sc-sampleservice-dev.yml]( https://github.com/zhangdberic/config-repo/blob/master/sc-sampleservice/sc-sampleservice-dev.yml )，并注册sc-sampleservcie服务到eureka上。



提供了如下URL：

http://192.168.5.78:8000/1，GET请求，查看用户id为1的用户信息。

http://192.168.5.78:8005/user/1?sleep=5001，GET请求，提供了延时执行(服务方法内sleep)，例如:5001则延时5001秒，多用于测试超时。

http://192.168.5.78:8000/ ，POST请求，增加用户，请求的体json，{"username":"heige","name":"黑哥","age":39,"balance":10000000}，用postman提交比较好。

http://192.168.5.78:8000/uploadFile ，POST请求(form-data格式)，请求参数名file，参数值为上传文件。



http://192.168.5.78:8000/health，监控检查可以查看到：db、rabbitmq、config、eureka、hystrix等状态，比较全。



在测试环境上，可以部署两个sc-sampleservice服务。用于测试如下：

1.eureka上是否可以正确的识别两个服务实例。可以通过eureka主页面来验证，http://192.168.5.78:8070/

2.sc-sampleservice服务提供者角色，供ribbon测试负载均衡请求，sc-ribbon-test服务对sc-sampleservice服务发起请求，两个sc-sampleservice服务实例轮番被请求到。



## docker启动脚本

docker run -itd --cap-add=SYS_PTRACE --name sc-microservice-sample1 --net host -e JAVA_OPTS="-Xms100m -Xmx100m -Xmn60m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev --server.port=8000" dyit.com:5000/sc/sc-microservice-sample:1.0.1

docker run -itd --cap-add=SYS_PTRACE --name sc-microservice-sample2 --net host -e JAVA_OPTS="-Xms100m -Xmx100m -Xmn60m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC" -e APP_ENV="--spring.profiles.active=dev --server.port=8001" dyit.com:5000/sc/sc-microservice-sample:1.0.1



