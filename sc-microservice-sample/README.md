# 服务提供者测试例子



其会在启动的时候通过config来获取git上的配置 [sc-sampleservice-dev.yml]( https://github.com/zhangdberic/config-repo/blob/master/sc-sampleservice/sc-sampleservice-dev.yml )，并注册sc-sampleservcie服务到eureka上。



提供了一个测试URL:http://192.168.5.78:8000/1，查看用户id为1的用户信息。



http://192.168.5.78:8000/health，监控检查可以查看到：db、rabbitmq、config、eureka、hystrix等状态，比较全。